package com.leysoft.products.domain

import data.*
import com.leysoft.core.kernel.context.contextual.*

object reader:
   trait ProductIdReader[F[_]]:
      def findBy(id: ProductId): Kind[F, Option[Product]]
   object ProductIdReader:
      inline def apply[F[_]](using
        F: ProductIdReader[F]
      ): ProductIdReader[F] =
        F
   trait ProductNameReader[F[_]]:
      def findBy(name: ProductName): Flow[F, Product]
   object ProductNameReader:
      inline def apply[F[_]](using
        F: ProductNameReader[F]
      ): ProductNameReader[F] =
        F
   trait ProductReader[F[_]]:
      def findAll: Flow[F, Product]

   object ProductReader:
      inline def apply[F[_]](using
        F: ProductReader[F]
      ): ProductReader[F] =
        F
