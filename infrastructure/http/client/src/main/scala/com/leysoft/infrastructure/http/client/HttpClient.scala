package com.leysoft.infrastructure.http.client

import com.leysoft.core.kernel.context.contextual.*
import com.leysoft.core.kernel.error.data.BusinessError
import org.http4s.{EntityDecoder, MediaType, Request, Response, Status, Uri}

trait HttpClient[F[_]]:
   def status(uri: Uri): Kind[F, Status]
   def status(request: Request[F]): Kind[F, Status]
   def get[A](uri: Uri)(f: Response[F] => F[A]): Kind[F, A]
   def run[A](request: Request[F])(
     f: Response[F] => F[A]
   ): Kind[F, A]
   def expect[A](request: Request[F])(using
     D: EntityDecoder[F, A]
   ): Kind[F, A]
   def expectOption[A](request: Request[F])(using
     D: EntityDecoder[F, A]
   ): Kind[F, Option[A]]
   def expectOr[A](request: Request[F])(
     onError: Response[F] => F[Throwable]
   )(using D: EntityDecoder[F, A]): Kind[F, A]
   def expectOptionOr[A](request: Request[F])(
     onError: Response[F] => F[Throwable]
   )(using D: EntityDecoder[F, A]): Kind[F, Option[A]]
   def stream(req: Request[F]): Flow[F, Response[F]]

object HttpClient:
   inline def apply[F[_]](using F: HttpClient[F]): HttpClient[F] =
     summon[HttpClient[F]]

   case class HttpClientError(message: String, code: String)
       extends BusinessError(message, code)

   object HttpClientError:
      inline def apply(message: String): BusinessError =
        HttpClientError(message, "00050")
