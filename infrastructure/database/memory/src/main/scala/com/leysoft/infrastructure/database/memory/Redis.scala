package com.leysoft.infrastructure.database.memory

import cats.effect.Async
import cats.syntax.applicative.*
import cats.syntax.apply.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import com.leysoft.core.kernel.context.contextual.*
import com.leysoft.core.logger.Logger
import fs2.Stream
import dev.profunktor.redis4cats.RedisCommands
import org.typelevel.log4cats.StructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import scala.concurrent.duration.FiniteDuration

trait Redis[F[_]]:
   def hmGet[A](using D: Redis.Decoder[A])(
     key: String,
     fields: String*
   ): Contextual[F[Option[A]]]
   def hmSet[A](using
     E: Redis.Encoder[A]
   )(key: String, value: A): Contextual[F[Unit]]
   def hGet(key: String, field: String): Contextual[F[Option[String]]]
   def hSet(
     key: String,
     field: String,
     value: String
   ): Contextual[F[Boolean]]
   def hDel(key: String, fields: String*): Contextual[F[Long]]
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

   given [F[_]](using
     F: Async[F],
     R: RedisCommands[F, String, String]
   ): Redis[F] with
      given StructuredLogger[F] = Slf4jLogger.getLogger[F]
      override def hmGet[A](using D: Redis.Decoder[A])(
        key: String,
        fields: String*
      ): Contextual[F[Option[A]]] =
        Logger[F].info(s"hmGet: $key") *> R
          .hmGet(key, fields.distinct*)
          .map(Redis.Decoder[A].decode)
      override def hmSet[A](using E: Redis.Encoder[A])(
        key: String,
        value: A
      ): Contextual[F[Unit]] =
        Logger[F].info(s"hmSet: $key") *>
          Redis
            .Encoder[A]
            .encode(value)
            .pure[F]
            .flatMap(R.hmSet(key, _))
      override def hGet(
        key: String,
        field: String
      ): Contextual[F[Option[String]]] =
        Logger[F].info(s"hGet: $key") *> R.hGet(key, field)
      override def hSet(
        key: String,
        field: String,
        value: String
      ): Contextual[F[Boolean]] =
        Logger[F].info(s"hSet: $key") *> R.hSet(key, field, value)
      override def hDel(
        key: String,
        fields: String*
      ): Contextual[F[Long]] =
        Logger[F].info(s"hDel: $key") *> R.hDel(key, fields.distinct*)
      override def expire(
        key: String,
        expiration: FiniteDuration
      ): Contextual[F[Boolean]] =
        Logger[F].info(s"expire: $key") *> R.expire(key, expiration)
