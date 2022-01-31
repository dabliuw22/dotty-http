package com.leysoft.infrastructure.http.server

import cats.effect.{Async, Resource}
import cats.syntax.functor.*
import cats.syntax.semigroupk.*
import com.leysoft.infrastructure.http.server.middleware.ContextMiddleware
import com.leysoft.infrastructure.http.server.routes.HealthRoute
import config.HttpServerConfiguration
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.blaze.server.*
import org.http4s.implicits.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.server.Server
import org.typelevel.log4cats.Logger

import scala.concurrent.ExecutionContext

object HttpServerResource:
   inline def apply[F[_]](using
     F: Async[F],
     L: Logger[F]
   ): HttpServerConfiguration ?=> ExecutionContext ?=> HttpRoutes[
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
             .apply((HealthRoute[F].routes <+> app).orNotFound)
         )
         .resource
         .preAllocate(Logger[F].info("Acquire Server..."))
         .onFinalize(Logger[F].info("Release Server..."))
