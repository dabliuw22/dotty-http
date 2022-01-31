package com.leysoft.core.kernel.context

import cats.{Eq, Order, Show}
import cats.derived.semiauto.*
import cats.syntax.show.*
import com.leysoft.core.kernel.newtype.instances.given
import io.circe.{Decoder, Encoder}
import io.circe.syntax.*
import io.circe.Codec
import fs2.Stream

import java.time.ZonedDateTime
import java.util.UUID

object data:
   /*
   import cats.derived.semiauto.*
   import com.leysoft.core.kernel.newtype.data.*
   type ContextId = ContextId.Type
   object ContextId extends Newtype[String]:
      inline def make: ContextId = ContextId(UUID.randomUUID.toString)

   type ContextCreatedAt = ContextCreatedAt.Type
   object ContextCreatedAt extends Newtype[ZonedDateTime]:
      inline def make: ContextCreatedAt = ContextCreatedAt(
        ZonedDateTime.now
      )
    */

   case class ContextId(value: String)
       derives Eq,
         Show,
         Order,
         Codec.AsObject
   object ContextId:
      inline def make: ContextId = ContextId(UUID.randomUUID.toString)

   case class Context(
     id: ContextId
   ) derives Eq,
         Show,
         Order,
         Codec.AsObject

   object Context:
      inline def make: Context =
        Context(ContextId.make)

      inline def from(id: ContextId): Context =
        Context(id)
