package com.leysoft.products.adapters.sql

import cats.effect.Async
import cats.syntax.applicative.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import com.leysoft.core.kernel.context.contextual.*
import com.leysoft.infrastructure.database.sql.skunk.*
import com.leysoft.products.adapters.sql.data.*
import com.leysoft.products.domain.data.*
import com.leysoft.products.domain.writer.*
import com.leysoft.products.domain.reader.*
import org.typelevel.log4cats.StructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import skunk.Void

object instances:
   given [F[_]](using F: Async[F], S: Skunk[F]): ProductIdReader[F]
     with
      given StructuredLogger[F] = Slf4jLogger.getLogger[F]
      override def findBy(
        id: ProductId
      ): Kind[F, Option[Product]] =
        S.option[ProductRow, String](byId, id.value)
          .map(_.map(row => row.to))

   given [F[_]](using F: Async[F], S: Skunk[F]): ProductNameReader[F]
     with
      given StructuredLogger[F] = Slf4jLogger.getLogger[F]
      override def findBy(
        name: ProductName
      ): Flow[F, Product] =
        S.stream[ProductRow, String](byName, name.value)
          .map(_.to)

   given [F[_]](using
     F: Async[F],
     S: Skunk[F]
   ): ProductReader[F] with
      given StructuredLogger[F] = Slf4jLogger.getLogger[F]
      override def findAll: Flow[F, Product] =
        S.stream[ProductRow, Void](all, Void)
          .map(_.to)

   given [F[_]](using F: Async[F], S: Skunk[F]): ProductWriter[F] with
      given StructuredLogger[F] = Slf4jLogger.getLogger[F]
      override def save(product: Product): Kind[F, Unit] =
        product
          .toRow
          .pure[F]
          .flatMap(S.command[ProductRow](create, _))
          .as(())
