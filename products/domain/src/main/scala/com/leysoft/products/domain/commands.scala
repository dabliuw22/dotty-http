package com.leysoft.products.domain

import com.leysoft.core.kernel.context.data.Context
import com.leysoft.core.kernel.message.data.*
import data.*

object commands:
   case class CreateProduct(
     override val id: MessageId,
     override val metadata: MessageMetadata,
     override val context: Context,
     name: ProductName,
     stock: ProductStock
   ) extends Command

   object CreateProduct:
      inline def make(using context: Context)(
        metadata: MessageMetadata
      )(name: ProductName, stock: ProductStock): CreateProduct =
        CreateProduct(
          MessageId(),
          metadata,
          context,
          name,
          stock
        )
