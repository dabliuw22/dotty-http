package com.leysoft.infrastructure.http.server

import cats.effect.{Async, ExitCode, Resource}
import cats.syntax.functor.*
import cats.syntax.semigroupk.*
import config.HttpServerConfiguration
import org.http4s.HttpApp
import org.http4s.blaze.server.*
import org.http4s.implicits.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.server.{Router, Server}
import scala.concurrent.ExecutionContext

object HttpServer:
   inline def apply[F[_]](using
     F: Async[F]
   ): ExecutionContext => HttpServerConfiguration => HttpApp[
     F
   ] => Resource[
     F,
     Server
   ] =
     ctx =>
       config =>
         app =>
           BlazeServerBuilder[F]
             .withExecutionContext(ctx)
             .bindHttp(config.port.value, config.host.value)
             .withHttpApp(
               CORS
                 .policy
                 .withAllowOriginAll
                 .withAllowCredentials(false)
                 .apply(app)
             )
             .resource
