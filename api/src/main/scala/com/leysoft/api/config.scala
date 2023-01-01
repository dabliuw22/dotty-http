package com.leysoft.api

import cats.effect.{Async, Resource}
import ciris.*
import ciris.refined.*
import com.leysoft.core.kernel.env.FromEnv
import eu.timepit.refined.types.string.NonEmptyString

object config:
   case class ApiConfiguration(clientId: NonEmptyString)

   object ApiConfiguration:
      inline given [F[_]: Async]: FromEnv[F, ApiConfiguration] with
         override def load: Resource[F, ApiConfiguration] =
           env("API_CLIENT_ID")
             .as[NonEmptyString]
             .map(ApiConfiguration(_))
             .resource
