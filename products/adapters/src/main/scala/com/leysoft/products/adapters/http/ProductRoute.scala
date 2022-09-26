package com.leysoft.products.adapters.http

import cats.effect.Async
import com.leysoft.infrastructure.http.server.middleware.context.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

final class ProductRoute[F[_]](using F: Async[F])
    extends Http4sDsl[F]:
   def routes: HttpRoutes[F] = ???
