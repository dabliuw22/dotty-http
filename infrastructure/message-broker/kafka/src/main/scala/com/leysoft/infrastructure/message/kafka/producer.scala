package com.leysoft.infrastructure.message.kafka

import cats.MonadThrow
import cats.effect.Async
import cats.syntax.applicative.*
import cats.syntax.apply.*
import cats.syntax.functor.*
import cats.syntax.flatMap.*
import cats.syntax.monadError.*
import com.leysoft.core.kernel.context.contextual.*
import com.leysoft.core.kernel.error.data.*
import com.leysoft.core.kernel.message.Producer
import com.leysoft.core.kernel.message.Producer.ProducerError
import com.leysoft.core.kernel.message.data.*
import com.leysoft.core.logger.Logger
import fs2.kafka.*
import fs2.Stream
import org.typelevel.log4cats.StructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object producer:
   given [F[_]](using
     F: Async[F],
     M: MonadThrow[F],
     P: KafkaProducer[F, String, Message],
     S: ProducerSettings[F, String, Message]
   ): Producer[F] with
      given StructuredLogger[F] = Slf4jLogger.getLogger[F]
      override def execute[A <: Message](
        message: A
      ): Contextual[F[MessageMetadata]] =
        Stream
          .emit(message)
          .covary[F]
          .map(record =>
            ProducerRecord(
              record.metadata.channel.value,
              record.metadata.key.value,
              record
            )
          )
          .map(record => ProducerRecords.one(record))
          .through(KafkaProducer.pipe(S, P))
          .as(message.metadata)
          .compile
          .toList
          .adaptError(error => ProducerError(error.getMessage))
          .flatMap {
            case head :: _ => head.pure[F]
            case Nil       =>
              Async[F].raiseError(
                ProducerError(
                  s"Message: ${message.getClass} not published"
                )
              )
          } <* Logger[F].info(s"Publish message: ${message.getClass}")
