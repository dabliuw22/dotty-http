package com.leysoft.infrastructure.message.kafka

import cats.MonadThrow
import cats.effect.{Async, Ref}
import cats.syntax.applicative.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import com.leysoft.core.kernel.context.data.Context
import com.leysoft.core.kernel.context.contextual.ContextualStream
import com.leysoft.core.kernel.message.data.Message
import com.leysoft.core.kernel.message.{Handler, Consumer}
import com.leysoft.core.logger.Logger
import fs2.kafka.*
import fs2.Stream
import org.typelevel.log4cats.StructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import scala.concurrent.duration.*

object consumer:
   given [F[_]](using
     F: Async[F],
     M: MonadThrow[F],
     P: KafkaConsumer[F, String, Message],
     S: ConsumerState[F]
   ): Consumer[F] with
      given StructuredLogger[F] = Slf4jLogger.getLogger[F]
      override def execute(channels: String*): Stream[F, Unit] =
        Stream
          .emit(P)
          .covary[F]
          .evalTap(_.subscribeTo(channels.head, channels.tail*))
          .flatMap(_.stream)
          .flatMap(reduce)
          .map(message => message.offset)
          .through(commitBatchWithin(100, 500 milliseconds))
      private def reduce(
        message: CommittableConsumerRecord[F, String, Message]
      ) =
        S.execute(message.record.value)
          .fold(())((_, _) => ())
          .as(message)

   trait ConsumerState[F[_]]:
      def register[A <: Message](
        key: Class[A],
        value: Handler[F]
      ): F[Unit]
      def execute[A <: Message](message: A): Stream[F, Unit]

   object ConsumerState:
      inline def apply[F[_]](using
        F: ConsumerState[F]
      ): ConsumerState[F] = F

      type State[F[_]] = Ref[F, Map[Class[?], List[Handler[F]]]]

      given [F[_]: Async](using S: State[F]): ConsumerState[F] with
         given StructuredLogger[F] = Slf4jLogger.getLogger[F]
         override def register[A <: Message](
           key: Class[A],
           value: Handler[F]
         ): F[Unit] =
           S.get
             .flatMap(
               _.get(key).fold(S.update(_.updated(key, List(value))))(
                 values =>
                   S.update(_.updated(key, values.appended(value)))
               )
             )
         override def execute[A <: Message](
           message: A
         ): Stream[F, Unit] =
            given Context = message.context
            Stream
              .eval(S.get.map(_.get(message.getClass)))
              .flatMap {
                case Some(handlers) =>
                  fs2
                    .Stream
                    .emits(handlers)
                    .covary[F]
                    .flatMap { handler =>
                      handler
                        .execute(message)
                        .handleErrorWith(e =>
                          error(
                            s"Handler: ${handler.getClass}, error consuming: ${message.getClass}",
                            e
                          )
                        )
                    }
                case _              =>
                  error(
                    s"Error: There are no handlers for: ${message.getClass}"
                  )
              }
         private def error(
           message: String
         ): ContextualStream[F, Unit] =
           Stream.eval(Logger[F].error(message))

         private def error(
           message: String,
           cause: Throwable
         ): ContextualStream[F, Unit] =
           Stream.eval(Logger[F].error(message)(cause))
