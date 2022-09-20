package com.leysoft.products.application.handler.command

import cats.effect.Async
import com.leysoft.core.kernel.context.contextual.*
import com.leysoft.core.kernel.event.ProductCreated
import com.leysoft.core.kernel.message.*
import com.leysoft.core.kernel.message.data.*
import com.leysoft.products.domain.commands.CreateProduct
import com.leysoft.products.domain.data.*
import com.leysoft.products.domain.reader.ProductNameReader
import org.typelevel.log4cats.StructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

final class CreateProductHandler[F[
  _
]: Async: ContextHandler](using R: ProductNameReader[F])(
  channel: MessageChannel
) extends Handler[F]:
   given StructuredLogger[F] = Slf4jLogger.getLogger[F]
   override def execute[A <: data.Message](
     message: A
   ): Flow[F, data.Message] =
     message match
        case CreateProduct(_, _, _, name, stock) =>
          R.findBy(name)
            .as(message)
            .ifEmpty(
              fs2
                .Stream
                .emit(Product.make(name, stock))
                .map(_.toKernel)
                .map(
                  ProductCreated.make(
                    MessageMetadata.make(channel = channel)
                  )(_)
                )
            )
        case _                                   => fs2.Stream.empty
