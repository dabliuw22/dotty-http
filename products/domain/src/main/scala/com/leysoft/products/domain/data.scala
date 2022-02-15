package com.leysoft.products.domain

import java.time.ZonedDateTime
import java.util.UUID

object data:
   case class ProductId(value: String)    extends AnyVal
   object ProductId:
      inline def make: ProductId = ProductId(UUID.randomUUID.toString)
   case class ProductName(value: String)  extends AnyVal
   case class ProductStock(value: Double) extends AnyVal
   case class ProductCreatedAt(
     value: ZonedDateTime
   ) extends AnyVal
   object ProductCreatedAt:
      inline def make: ProductCreatedAt = ProductCreatedAt(
        ZonedDateTime.now
      )
   case class Product(
     id: ProductId,
     name: ProductName,
     stock: ProductStock,
     createdAt: ProductCreatedAt
   )
   object Product:
      inline def make(
        name: ProductName,
        stock: ProductStock
      ): Product =
        Product(ProductId.make, name, stock, ProductCreatedAt.make)
