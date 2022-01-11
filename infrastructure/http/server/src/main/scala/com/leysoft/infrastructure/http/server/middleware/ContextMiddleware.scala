package com.leysoft.infrastructure.http.server.middleware

import cats.data.Kleisli
import cats.effect.Async
import cats.syntax.applicative.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import com.leysoft.core.kernel.context.contextual.Contextual
import com.leysoft.core.kernel.context.data.{Context, ContextId}
import com.leysoft.core.logger.algebra.ContextHandler
import org.http4s.{Header, Http, Request, Response}
import org.typelevel.ci.*

object ContextMiddleware:
   private val XCorrelationId = ci"X-Correlation-Id"

   def apply[F[_]: Async](request: Request[F])(
     f: Contextual[F[Response[F]]]
   ): F[Response[F]] =
     for
        context <-
          request
            .headers
            .get(XCorrelationId)
            .map(_.head)
            .fold(Context.make)(result =>
              Context.from(ContextId(result.value))
            )
            .pure[F]
        header = Header.Raw(XCorrelationId, context.id.value)
        response <- f(using context)
     yield response.putHeaders(header)
