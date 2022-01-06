package com.leysoft.infrastructure.database.sql.skunk

import cats.effect.{Concurrent, Async, Resource}
import cats.effect.std.Console
import cats.syntax.parallel.*
import ciris.*
import ciris.refined.*
import com.leysoft.core.kernel.env.FromEnv
import eu.timepit.refined.auto.*
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.string.NonEmptyString
import natchez.Trace
import natchez.Trace.Implicits.noop
import skunk.{Session, SessionPool}

object config:
   type ThreadSize = PosInt
   case class SkunkConfiguration(
     host: NonEmptyString,
     port: UserPortNumber,
     user: NonEmptyString,
     password: NonEmptyString,
     database: NonEmptyString,
     threadSize: ThreadSize
   )

   object SkunkConfiguration:
      given [F[_]: Async]: FromEnv[F, SkunkConfiguration] with
         override def load: Resource[F, SkunkConfiguration] =
           (
             env("DATABASE_HOST")
               .as[NonEmptyString],
             env("DATABASE_PORT")
               .as[UserPortNumber],
             env("DATABASE_USER")
               .as[NonEmptyString],
             env("DATABASE_PASSWORD")
               .as[NonEmptyString],
             env("DATABASE_NAME")
               .as[NonEmptyString],
             env("DATABASE_THREAD_SIZE")
               .as[ThreadSize]
           ).parMapN {
             (host, port, user, password, database, threadSize) =>
               SkunkConfiguration(
                 host,
                 port,
                 user,
                 password,
                 database,
                 threadSize
               )
           }.resource
   extension (conf: SkunkConfiguration)
      def single[F[_]: Async: Console]: Resource[F, Session[F]] =
        Session.single[F](
          host = conf.host.value,
          port = conf.port.value,
          user = conf.user.value,
          password = Some(conf.password.value),
          database = conf.database.value
        )
      def pool[F[_]: Async: Console]: SessionPool[F]            =
        Session.pooled(
          host = conf.host,
          port = conf.port,
          user = conf.user,
          password = Some(conf.password),
          database = conf.database,
          max = conf.threadSize
        )
