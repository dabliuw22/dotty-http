package com.leysoft.products.domain

import com.leysoft.core.kernel.domain.product.data.ProductId as KernelProductId
import com.leysoft.core.kernel.domain.product.data.ProductName as KernelProductName
import com.leysoft.core.kernel.domain.product.data.ProductStock as KernelProductStock
import com.leysoft.core.kernel.domain.product.data.ProductCreatedAt as KernelProductCreatedAt
import com.leysoft.core.kernel.domain.product.data.Product as KernelProduct

import java.time.ZonedDateTime
import java.util.UUID

object data:
   case class ProductId(value: String)    extends AnyVal:
      def toKernel: KernelProductId = KernelProductId(value)
   object ProductId:
      inline def make: ProductId = ProductId(UUID.randomUUID.toString)
   case class ProductName(value: String)  extends AnyVal:
      def toKernel: KernelProductName = KernelProductName(value)
   case class ProductStock(value: Double) extends AnyVal:
      def toKernel: KernelProductStock = KernelProductStock(value)
   case class ProductCreatedAt(
     value: ZonedDateTime
   ) extends AnyVal:
      def toKernel: KernelProductCreatedAt = KernelProductCreatedAt(
        value
      )
   object ProductCreatedAt:
      inline def make: ProductCreatedAt = ProductCreatedAt(
        ZonedDateTime.now
      )
   case class Product(
     id: ProductId,
     name: ProductName,
     stock: ProductStock,
     createdAt: ProductCreatedAt
   ):
      def toKernel: KernelProduct = KernelProduct(
        id = id.toKernel,
        name = name.toKernel,
        stock = stock.toKernel,
        createdAt = createdAt.toKernel
      )
   object Product:
      inline def make(
        name: ProductName,
        stock: ProductStock
      ): Product =
        Product(ProductId.make, name, stock, ProductCreatedAt.make)

   extension (product: KernelProduct)
     def to: Product =
       Product(
         ProductId(product.id.value),
         ProductName(product.name.value),
         ProductStock(product.stock.value),
         ProductCreatedAt(product.createdAt.value)
       )
