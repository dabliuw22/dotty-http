package com.leysoft.infrastructure.http.server

import cats.effect.{Async, ExitCode, Resource}
import cats.syntax.functor.*
import cats.syntax.semigroupk.*
import com.leysoft.infrastructure.http.server.middleware.ContextMiddleware
import config.HttpServerConfiguration
import org.http4s.HttpApp
import org.http4s.blaze.server.*
import org.http4s.implicits.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.server.Server
import scala.concurrent.ExecutionContext

object HttpServer:
   inline def apply[F[_]](using
     F: Async[F]
   ): HttpServerConfiguration ?=> ExecutionContext ?=> HttpApp[
     F
   ] => Resource[
     F,
     Server
   ] =
     app =>
       BlazeServerBuilder[F]
         .withExecutionContext(summon[ExecutionContext])
         .bindHttp(
           summon[HttpServerConfiguration].port.value,
           summon[HttpServerConfiguration].host.value
         )
         .withBanner(Seq.empty)
         .withHttpApp(
           CORS
             .policy
             .withAllowOriginAll
             .withAllowCredentials(false)
             .apply(app)
         )
         .resource
