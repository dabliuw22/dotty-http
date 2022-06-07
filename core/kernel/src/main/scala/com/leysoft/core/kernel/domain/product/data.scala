package com.leysoft.core.kernel.domain.product

import java.time.ZonedDateTime
import java.util.UUID

object data:
   case class ProductId(value: String)    extends AnyVal
   case class ProductName(value: String)  extends AnyVal
   case class ProductStock(value: Double) extends AnyVal
   case class ProductCreatedAt(
     value: ZonedDateTime
   ) extends AnyVal
   case class Product(
     id: ProductId,
     name: ProductName,
     stock: ProductStock,
     createdAt: ProductCreatedAt
   )
