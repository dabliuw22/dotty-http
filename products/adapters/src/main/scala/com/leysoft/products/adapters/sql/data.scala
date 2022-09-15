package com.leysoft.products.adapters.sql

import com.leysoft.products.domain.data.*
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*

import java.time.OffsetDateTime

object data:
   case class ProductRow(
     id: String,
     name: String,
     stock: Double,
     createdAt: OffsetDateTime
   ):
      def to: Product = Product(
        ProductId(id),
        ProductName(name),
        ProductStock(stock),
        ProductCreatedAt(createdAt.toZonedDateTime)
      )

   extension (product: Product)
     def toRow: ProductRow = ProductRow(
       product.id.value,
       product.name.value,
       product.stock.value,
       product.createdAt.value.toOffsetDateTime
     )

   val all: Query[Void, ProductRow]      =
     sql"SELECT * FROM products"
       .query(varchar ~ varchar ~ float8 ~ timestamptz)
       .map { case id ~ name ~ stock ~ createdAt =>
         ProductRow(id, name, stock, createdAt)
       }
   val byId: Query[String, ProductRow]   =
     sql"SELECT * FROM products WHERE id = $varchar"
       .query(varchar ~ varchar ~ float8 ~ timestamptz)
       .map { case id ~ name ~ stock ~ createdAt =>
         ProductRow(id, name, stock, createdAt)
       }
   val byName: Query[String, ProductRow] =
     sql"SELECT * FROM products WHERE name = $varchar"
       .query(varchar ~ varchar ~ float8 ~ timestamptz)
       .map { case id ~ name ~ stock ~ createdAt =>
         ProductRow(id, name, stock, createdAt)
       }
   val create: Command[ProductRow]       =
     sql"""INSERT INTO products(id, name, stock, created_at)
           VALUES($varchar, $varchar, $float8, $timestamptz)"""
       .command
       .contramap { case ProductRow(i, n, s, c) =>
         i ~ n ~ s ~ c
       }
