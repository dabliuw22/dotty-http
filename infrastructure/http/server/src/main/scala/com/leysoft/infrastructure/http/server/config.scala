package com.leysoft.infrastructure.http.server

import cats.effect.{Async, Resource}
import cats.syntax.parallel.*
import ciris.*
import ciris.refined.*
import com.leysoft.core.kernel.env.FromEnv
import eu.timepit.refined.auto.*
import eu.timepit.refined.cats.*
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.string.NonEmptyString

object config:
   case class HttpServerConfiguration(
     host: NonEmptyString,
     port: UserPortNumber
   )

   object HttpServerConfiguration:
      given [F[_]: Async]: FromEnv[F, HttpServerConfiguration] with
         override def load: Resource[F, HttpServerConfiguration] =
           (
             env("SERVER_HOST")
               .as[NonEmptyString],
             env("SERVER_PORT")
               .as[UserPortNumber]
           ).parMapN { (host, port) =>
             HttpServerConfiguration(host, port)
           }.resource
