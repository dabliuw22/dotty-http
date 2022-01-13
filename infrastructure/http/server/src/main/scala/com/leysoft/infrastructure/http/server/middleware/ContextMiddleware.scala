package com.leysoft.infrastructure.http.server.middleware

import cats.data.Kleisli
import cats.effect.Async
import cats.syntax.applicative.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import com.leysoft.core.kernel.context.contextual.Contextual
import com.leysoft.core.kernel.context.data.*
import com.leysoft.infrastructure.http.kernel.headers.*
import org.http4s.{Http, Request, Response}
import org.http4s.Header

object ContextMiddleware:
   extension [F[_]: Async](request: Request[F])
     def handle(f: Contextual[F[Response[F]]]): F[Response[F]] =
       for
          context @ given Context <- request.getContext
          response                <- f
       yield response.withContext
