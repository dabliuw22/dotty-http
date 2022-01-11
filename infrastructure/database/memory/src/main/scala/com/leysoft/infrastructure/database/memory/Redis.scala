package com.leysoft.infrastructure.database.memory

import com.leysoft.core.kernel.context.contextual.*
import com.leysoft.core.kernel.error.data.BusinessError
import io.circe.{Decoder, Encoder}
import scala.concurrent.duration.FiniteDuration

trait Redis[F[_]]:
   def hmGet[A](using
     D: Redis.Decoder[A]
   )(key: String, fields: String*): Contextual[F[Option[A]]]
   def hmSet[A](using
     E: Redis.Encoder[A]
   )(key: String, value: A): Contextual[F[Unit]]
   def jsGet[A](using D: Decoder[A])(
     key: String
   ): Contextual[F[Option[A]]]
   def jsSet[A](using
     E: Encoder[A]
   )(key: String, value: A): Contextual[F[Unit]]
   def hGet(key: String, field: String): Contextual[F[Option[String]]]
   def hSet(
     key: String,
     field: String,
     value: String
   ): Contextual[F[Boolean]]
   def hDel(key: String, fields: String*): Contextual[F[Long]]
   def get(key: String): Contextual[F[Option[String]]]
   def set(key: String, value: String): Contextual[F[Unit]]
   def del(key: String): Contextual[F[Long]]
   def expire(
     key: String,
     expiration: FiniteDuration
   ): Contextual[F[Boolean]]

object Redis:
   inline def apply[F[_]](using F: Redis[F]): Redis[F] =
     summon[Redis[F]]

   trait Decoder[A]:
      def decode(row: Map[String, String]): Option[A]

   object Decoder:
      inline def apply[A](using D: Decoder[A]): Decoder[A] =
        summon[Decoder[A]]

   trait Encoder[A]:
      def encode(value: A): Map[String, String]

   object Encoder:
      inline def apply[A](using E: Encoder[A]): Encoder[A] =
        summon[Encoder[A]]

   case class RedisError(message: String, code: String)
       extends BusinessError(message, code)

   object RedisError:
      inline def apply(message: String): BusinessError =
        RedisError(message, "00010")
