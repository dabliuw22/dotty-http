package com.leysoft.infrastructure.message.kafka

import cats.effect.Async
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.{DeserializationFeature, SerializationFeature}
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.leysoft.core.kernel.message.data.Message
import com.sun.net.httpserver.Authenticator.Success
import fs2.kafka.Deserializer as KafkaDeserializer
import fs2.kafka.Serializer as KafkaSerializer
import java.util.Map as JavaMap
import org.apache.kafka.common.serialization.*
import scala.util.Try

object serde:
   object Json:
      private val Mapper = JsonMapper
        .builder
        .addModule(DefaultScalaModule)
        .addModule(new Jdk8Module)
        .addModule(new JavaTimeModule)
        .configure(
          DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
          false
        )
        .configure(
          SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
          false
        )
        .build

      def write[A <: Message](data: A): Option[String] = Try(
        Mapper.writeValueAsString(data)
      ).toOption
      def read[A <: Message](data: String): Option[A]  = Try(
        Mapper.readValue(data, classOf[Message]).asInstanceOf[A]
      ).toOption

   private class JsonSerde[A <: Message]
       extends Serializer[A]
       with Deserializer[A]
       with Serde[A]:
      private val ser            = new StringSerializer
      private val des            = new StringDeserializer
      override def configure(
        configs: JavaMap[String, ?],
        isKey: Boolean
      ): Unit = super.configure(configs, isKey)
      override def close(): Unit = super.close()
      override def serialize(topic: String, data: A): Array[Byte]   =
        Json.write(data) match
           case Some(json) => ser.serialize(topic, json)
           case _ => throw new RuntimeException("Write Error")
      override def deserialize(topic: String, data: Array[Byte]): A =
        Json.read(des.deserialize(topic, data)) match
           case Some(value) => value
           case _ => throw new RuntimeException("Read Error")
      override def serializer(): Serializer[A]     = this
      override def deserializer(): Deserializer[A] = this

   def keyDeserializer[F[_]: Async]: KafkaDeserializer[F, String]    =
     KafkaDeserializer[F, String]
   def valueDeserializer[F[_]: Async]: KafkaDeserializer[F, Message] =
     KafkaDeserializer.delegate[F, Message](JsonSerde())
   def keySerializer[F[_]: Async]: KafkaSerializer[F, String]        =
     KafkaSerializer[F, String]
   def valueSerializer[F[_]: Async]: KafkaSerializer[F, Message]     =
     KafkaSerializer.delegate[F, Message](JsonSerde())
