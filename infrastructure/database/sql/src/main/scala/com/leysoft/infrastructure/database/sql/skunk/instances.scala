package com.leysoft.infrastructure.database.sql.skunk

import cats.MonadThrow
import cats.effect.Async
import cats.syntax.apply.*
import cats.syntax.functor.*
import cats.syntax.monadError.*
import com.leysoft.core.kernel.context.contextual.*
import com.leysoft.core.kernel.error.data.BusinessError
import com.leysoft.core.logger.Logger
import org.typelevel.log4cats.StructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import skunk.*
import skunk.codec.all.*
import skunk.data.Completion
import skunk.implicits.*

object instances:
   given [F[_]](using F: Async[F], S: Session[F]): Skunk[F] with
      given StructuredLogger[F] = Slf4jLogger.getLogger[F]
      override def option[A, B](
        query: Query[B, A],
        args: B
      ): Contextual[F[Option[A]]] =
        Logger[F].info(s"Option: ${query.sql}") *> S
          .prepare(query)
          .use(_.option(args))
          .adaptError(error => Skunk.SkunkError(error.getMessage))
      override def stream[A, B](
        query: Query[B, A],
        args: B,
        size: Int = 64
      ): ContextualStream[F, A] =
        for
           _        <-
             fs2.Stream.eval(Logger[F].info(s"Stream: ${query.sql}"))
           prepared <- fs2.Stream.resource(S.prepare(query))
           result   <-
             prepared
               .stream(args, size)
               .adaptError(error =>
                 Skunk.SkunkError(error.getMessage)
               )
        yield result
      override def list[A, B](
        query: Query[B, A],
        args: B
      ): Contextual[F[List[A]]] =
        Logger[F].info(s"List: ${query.sql}") *> stream[A, B](
          query,
          args
        ).compile.toList
      override def command[A](
        command: Command[A],
        args: A
      ): Contextual[F[Int]] =
        Logger[F].info(s"Command: ${command.sql}") *>
          S.prepare(command)
            .use {
              _.execute(args)
                .map {
                  case Completion.Insert(rows) => rows
                  case Completion.Update(rows) => rows
                  case Completion.Delete(rows) => rows
                  case Completion.Copy(rows)   => rows
                  case _                       => 0
                }
            }
            .adaptError(error => Skunk.SkunkError(error.getMessage))
