package com.leysoft.core.kernel.message

import com.leysoft.core.kernel.error.data.BusinessError
import com.leysoft.core.kernel.message.data.MessageChannel
import fs2.Stream

trait Consumer[F[_]]:
   def execute(channels: MessageChannel*): Stream[F, Unit]

object Consumer:
   inline def apply[F[_]](using F: Consumer[F]): Consumer[F] = F
   case class ConsumerError(message: String, code: String)
       extends BusinessError(message, code)
   object ConsumerError:
      inline def apply(message: String): BusinessError =
        ConsumerError(message, "00040")
