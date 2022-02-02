package com.leysoft.infrastructure.http.client

import cats.effect.Async
import cats.data.Kleisli
import cats.syntax.applicative.*
import org.http4s.Response
import org.http4s.client.Client

object ClientTest:
   inline def apply[F[_]](using F: Async[F])(
     response: Response[F]
   ): Client[F] =
     Client.fromHttpApp[F](Kleisli(_ => F.pure(response)))

   inline def make[F[_]](using F: Async[F])(
     response: Response[F]
   ): F[Client[F]] =
     ClientTest.apply[F](response).pure[F]
