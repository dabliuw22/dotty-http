package com.leysoft.core.kernel.event

import com.leysoft.core.kernel.context.data.Context
import com.leysoft.core.kernel.domain.product.data.*
import com.leysoft.core.kernel.message.data.*

final case class ProductCreated(
  override val id: MessageId,
  override val metadata: MessageMetadata,
  override val context: Context,
  product: Product
) extends Event

object ProductCreated:
   inline def make(using context: Context)(
     metadata: MessageMetadata
   )(product: Product): ProductCreated =
     ProductCreated(MessageId(), metadata, context, product)
