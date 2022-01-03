package com.leysoft.infrastructure.database.sql.doobie

import cats.effect.{Async, Resource}
import cats.syntax.parallel.*
import ciris.*
import ciris.refined.*
import com.leysoft.core.kernel.env.FromEnv
import doobie.hikari.*
import doobie.util.ExecutionContexts
import eu.timepit.refined.auto.*
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.types.string.NonEmptyString

object config:
   type ThreadSize = Int Refined Interval.Open[0, 100]
   case class DoobieConfiguration(
     driver: NonEmptyString,
     url: NonEmptyString,
     user: NonEmptyString,
     password: NonEmptyString,
     threadSize: ThreadSize
   )

   object DoobieConfiguration:
      given [F[_]: Async]: FromEnv[F, DoobieConfiguration] with
         override def load: Resource[F, DoobieConfiguration] =
           (
             env("DATABASE_DRIVER")
               .as[NonEmptyString],
             env("DATABASE_URL")
               .as[NonEmptyString],
             env("DATABASE_USER")
               .as[NonEmptyString],
             env("DATABASE_PASSWORD")
               .as[NonEmptyString],
             env("DATABASE_THREAD_SIZE")
               .as[ThreadSize]
           ).parMapN { (driver, url, user, password, threadSize) =>
             DoobieConfiguration(
               driver,
               url,
               user,
               password,
               threadSize
             )
           }.resource

   extension (conf: DoobieConfiguration)
     def transactor[F[_]: Async]: Resource[F, HikariTransactor[F]] =
       for
          context <-
            ExecutionContexts.fixedThreadPool[F](conf.threadSize)
          hikari  <- HikariTransactor.newHikariTransactor[F](
                       driverClassName = conf.driver,
                       url = conf.url,
                       user = conf.user,
                       pass = conf.password,
                       connectEC = context
                     )
       yield hikari
