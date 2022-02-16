package com.leysoft.products.domain

import data.*
import com.leysoft.core.kernel.context.contextual.*

trait ProductReader[F[_]]:
   def findBy(id: ProductId): Contextual[F[Option[Product]]]
   def findAll: ContextualStream[F, Product]

object ProductReader:
   inline def apply[F[_]](using
     F: ProductReader[F]
   ): ProductReader[F] =
     F
