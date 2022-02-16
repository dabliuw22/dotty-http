package com.leysoft.products.domain

import data.*
import com.leysoft.core.kernel.context.contextual.*

trait ProductWriter[F[_]]:
   def save(product: Product): Contextual[F[Unit]]

object ProductWriter:
   inline def apply[F[_]](using
     F: ProductWriter[F]
   ): ProductWriter[F] =
     F
