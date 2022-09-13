package com.leysoft.products.domain

import data.*
import com.leysoft.core.kernel.context.contextual.*

object writer:
   trait ProductWriter[F[_]]:
      def save(product: Product): Kind[F, Unit]

   object ProductWriter:
      inline def apply[F[_]](using
        F: ProductWriter[F]
      ): ProductWriter[F] =
        F
