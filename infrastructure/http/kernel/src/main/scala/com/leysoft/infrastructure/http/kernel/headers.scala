package com.leysoft.infrastructure.http.kernel

import cats.Applicative
import cats.syntax.applicative.*
import com.leysoft.core.kernel.context.contextual.Contextual
import com.leysoft.core.kernel.context.data.*
import org.http4s.{Header, Headers, Http, Request, Response}
import org.typelevel.ci.*
import scala.annotation.targetName

object headers:
   val XCorrelationId = ci"X-Correlation-Id"

   extension (ctx: Context)
     def headers: Headers = Headers(
       List(Header.Raw(XCorrelationId, ctx.id.value))
     )

   extension [F[_]: Applicative](request: Request[F])
      def getContext: F[Context]                      =
        request
          .headers
          .get(XCorrelationId)
          .map(_.head)
          .fold(Context.make)(result =>
            Context.from(ContextId(result.value))
          )
          .pure[F]
      @targetName("appendFromContext")
      def ++> (ctx: Context): Request[F] =
        request.putHeaders(ctx.headers)
      def withContext(using ctx: Context): Request[F] =
        request ++> ctx

   extension [F[_]: Applicative](response: Response[F])
      @targetName("appendFromContext")
      def <++ (ctx: Context): Response[F] =
        response.putHeaders(ctx.headers)
      def withContext(using ctx: Context): Response[F] =
        response <++ ctx
