package com.leysoft.infrastructure.database.sql.doobie

import cats.MonadThrow
import cats.effect.Async
import cats.syntax.apply.*
import cats.syntax.monadError.*
import com.leysoft.core.kernel.context.contextual.*
import com.leysoft.core.kernel.error.data.BusinessError
import com.leysoft.core.logger.Logger
import doobie.ConnectionIO
import doobie.implicits.*
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import fs2.Stream
import org.typelevel.log4cats.StructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object instances:
   given [F[_]](using
     F: Async[F],
     M: MonadThrow[F],
     T: Transactor[F]
   ): Doobie[F] with
      given StructuredLogger[F] = Slf4jLogger.getLogger[F]
      override def option[T](
        query: Query0[T]
      ): Contextual[F[Option[T]]] =
        Logger[F].info(s"Option: ${query.sql}") *> transaction {
          query.option
        }
      override def stream[T](
        query: Query0[T]
      ): ContextualStream[F, T] =
        Stream.eval(Logger[F].info(s"Stream: ${query.sql}")) >> query
          .stream
          .transact(T)
          .adaptError(error => Doobie.DoobieError(error.getMessage))
      override def list[T](query: Query0[T]): Contextual[F[List[T]]] =
        Logger[F].info(s"List: ${query.sql}") *> transaction {
          query.stream.compile.toList
        }
      override def command(command: Update0): Contextual[F[Int]]     =
        Logger[F].info(s"Command: ${command.sql}") *>
          transaction[Int] { command.run }
      override def transaction[A](
        program: => ConnectionIO[A]
      ): Contextual[F[A]] =
        program
          .transact(T)
          .adaptError(error => Doobie.DoobieError(error.getMessage))
