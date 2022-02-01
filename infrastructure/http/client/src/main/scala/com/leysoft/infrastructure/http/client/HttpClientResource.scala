package com.leysoft.infrastructure.http.client

import cats.effect.{Async, Resource}
import com.leysoft.core.kernel.env.FromEnv
import com.leysoft.infrastructure.http.client.HttpBackend
import com.leysoft.infrastructure.http.client.instances.{*, given}
import org.http4s.client.Client
import org.typelevel.log4cats.Logger

object HttpClientResource:
   inline def apply[F[_]: Async](using L: Logger[F])(
     backend: HttpBackend
   ): Resource[F, HttpClient[F]] =
     for client @ given Client[F] <- backend.client[F]
     yield HttpClient[F]
