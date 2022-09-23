package com.leysoft.commands

import com.leysoft.core.kernel.context.data.*
import com.leysoft.core.kernel.message.data.*

import java.util.UUID

case class TestCommand(
  override val id: MessageId,
  override val metadata: MessageMetadata,
  override val context: Context,
  name: String
) extends Command

object TestCommand:
   def make(using
     ctx: Context
   )(name: String): Command =
     TestCommand(
       MessageId(),
       MessageMetadata(
         MessageChannel("commands.channel"),
         MessageKey(UUID.randomUUID.toString),
         MessageCreatedAt()
       ),
       ctx,
       name
     )
