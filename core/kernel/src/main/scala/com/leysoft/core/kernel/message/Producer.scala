package com.leysoft.core.kernel.message

import com.leysoft.core.kernel.context.contextual.*
import com.leysoft.core.kernel.error.data.BusinessError
import com.leysoft.core.kernel.message.data.*

trait Producer[F[_]]:
   def execute[A <: Message](
     message: A
   ): Contextual[F[MessageMetadata]]

object Producer:
   inline def apply[F[_]](using F: Producer[F]): Producer[F] = F
   case class ProducerError(message: String, code: String)
       extends BusinessError(message, code)
   object ProducerError:
      inline def apply(message: String): BusinessError =
        ProducerError(message, "00040")
