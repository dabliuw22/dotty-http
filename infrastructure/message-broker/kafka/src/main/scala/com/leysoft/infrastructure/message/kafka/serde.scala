package com.leysoft.infrastructure.message.kafka

import cats.effect.Async
import cats.syntax.applicative.*
import cats.syntax.applicativeError.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.leysoft.core.kernel.error.data.BusinessError
import com.leysoft.core.kernel.message.data.*
import com.sun.net.httpserver.Authenticator.Success
import fs2.kafka.Deserializer
import fs2.kafka.Serializer
import org.apache.kafka.common.serialization.*

import java.util.TimeZone
import scala.util.Try

object serde:
   private object Json:
      private val Mapper = JsonMapper
        .builder
        .addModule(DefaultScalaModule)
        .addModule(new Jdk8Module)
        .addModule(new JavaTimeModule)
        .configure(
          SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
          false
        )
        .configure(
          SerializationFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE,
          false
        )
        .configure(
          DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
          false
        )
        .configure(
          DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE,
          false
        )
        .configure(
          DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
          true
        )
        .build
        .setTimeZone(TimeZone.getDefault)

      def write[A <: Message](data: A): Option[String] = Try(
        Mapper.writeValueAsString(data)
      ).toOption
      def read[A <: Message](data: String): Option[A]  = Try(
        Mapper.readValue(data, classOf[Message]).asInstanceOf[A]
      ).toOption

   case class SerializerError(message: String, code: String)
       extends BusinessError(message, code)
   object SerializerError:
      inline def apply(message: String): BusinessError =
        SerializerError(message, "00040")
   case class DeserializerError(message: String, code: String)
       extends BusinessError(message, code)
   object DeserializerError:
      inline def apply(message: String): BusinessError =
        DeserializerError(message, "00040")

   // import fs2.kafka.Deserializer.given
   given [F[_]: Async]: Deserializer[F, String] =
     Deserializer.string[F]

   given [F[_]: Async](using
     D: Deserializer[F, String]
   ): Deserializer[F, Message] =
     Deserializer.instance { (topic, header, data) =>
       D
         .deserialize(topic, header, data)
         .map(Json.read[Message])
         .flatMap(
           _.fold(
             Async[F]
               .raiseError(
                 DeserializerError("Could not deserialize record")
               )
           )(_.pure[F])
         )
     }

   // import fs2.kafka.Serializer.given
   given [F[_]: Async]: Serializer[F, String] = Serializer.string[F]

   given [F[_]: Async](using
     S: Serializer[F, String]
   ): Serializer[F, Message] =
     Serializer.instance[F, Message] { (topic, header, data) =>
       Json
         .write[Message](data)
         .pure[F]
         .flatMap(
           _.fold(
             Async[F].raiseError(
               SerializerError("Could not serialize record")
             )
           )(_.pure[F])
         )
         .flatMap(
           S.serialize(topic, header, _)
         )
     }
