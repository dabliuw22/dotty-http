package com.leysoft.infrastructure.http.server.routes

import cats.effect.Async
import cats.syntax.applicative.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import io.circe.Encoder
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.*

class HealthRoute[F[_]](using F: Async[F]) extends Http4sDsl[F]:
   def routes: HttpRoutes[F] = HttpRoutes
     .of[F] { case GET -> Root / "health" =>
       HealthRoute
         .HealthResponse
         .ok
         .pure[F]
         .map(_.asJson)
         .flatMap(Ok(_))
     }

object HealthRoute:
   case class HealthResponse(status: String)
   object HealthResponse:
      given Encoder[HealthResponse] = deriveEncoder[HealthResponse]
      inline def ok: HealthResponse = HealthResponse("OK")
