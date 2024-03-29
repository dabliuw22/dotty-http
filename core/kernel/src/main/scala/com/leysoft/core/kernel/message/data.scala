package com.leysoft.core.kernel.message

import com.leysoft.core.kernel.context.data.Context
import com.fasterxml.jackson.annotation.JsonTypeInfo

import java.time.ZonedDateTime
import java.util.UUID

object data:
   case class MessageChannel(value: String)
   object MessageChannel:
      extension (s: String)
        inline def toChannel: MessageChannel = MessageChannel(s)
   case class MessageKey(value: String = UUID.randomUUID.toString)
   case class MessageCreatedAt(
     value: ZonedDateTime = ZonedDateTime.now
   )
   case class MessageId(value: String = UUID.randomUUID.toString)
   final case class MessageMetadata(
     channel: MessageChannel,
     key: MessageKey,
     createdAt: MessageCreatedAt
   )
   object MessageMetadata:
      inline def make(
        channel: MessageChannel,
        key: MessageKey = MessageKey()
      ): MessageMetadata =
        MessageMetadata(channel, key, MessageCreatedAt())
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
