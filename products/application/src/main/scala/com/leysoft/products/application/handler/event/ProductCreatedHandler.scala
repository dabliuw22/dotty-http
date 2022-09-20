package com.leysoft.products.application.handler.event

import cats.effect.Async
import com.leysoft.core.kernel.context.contextual.*
import com.leysoft.core.kernel.event.ProductCreated
import com.leysoft.core.kernel.message.data.MessageChannel
import com.leysoft.core.kernel.message.{Handler, data}
import com.leysoft.products.domain.data.*
import com.leysoft.products.domain.writer.ProductWriter
import org.typelevel.log4cats.StructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

final class ProductCreatedHandler[F[_]: Async: ContextHandler](using
  W: ProductWriter[F]
)(
  channel: MessageChannel
) extends Handler[F]:
   given StructuredLogger[F] = Slf4jLogger.getLogger[F]
   override def execute[A <: data.Message](
     message: A
   ): Flow[F, data.Message] =
     message match
        case ProductCreated(_, _, _, product) =>
          fs2
            .Stream
            .emit(product)
            .map(_.to)
            .evalMap(W.save)
            .as(message)
        case _                                => fs2.Stream.empty
