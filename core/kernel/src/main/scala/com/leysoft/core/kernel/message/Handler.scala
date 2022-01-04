package com.leysoft.core.kernel.message

import com.leysoft.core.kernel.context.contextual.*
import com.leysoft.core.kernel.message.data.*

trait Handler[F[_]]:
   def execute[A <: Message](message: A): ContextualStream[F, Unit]
