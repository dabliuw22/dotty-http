package com.leysoft.core.kernel.message

import com.leysoft.core.kernel.context.data.Context
import com.leysoft.core.kernel.newtype.instances.given
import com.fasterxml.jackson.annotation.JsonTypeInfo

import java.time.ZonedDateTime
import java.util.UUID

object data:
   case class MessageChannel(value: String)
   case class MessageKey(value: String)
   case class MessageCreatedAt(
     value: ZonedDateTime = ZonedDateTime.now
   )
   case class MessageId(value: String = UUID.randomUUID.toString)
   case class MessageMetadata(
     channel: MessageChannel,
     key: MessageKey,
     createdAt: MessageCreatedAt
   )
   @JsonTypeInfo(
     use = JsonTypeInfo.Id.CLASS,
     include = JsonTypeInfo.As.PROPERTY,
     property = "@type"
   )
   abstract class Message:
      def id: MessageId
      def metadata: MessageMetadata
      def context: Context
   abstract class Command extends Message
   abstract class Event   extends Message
