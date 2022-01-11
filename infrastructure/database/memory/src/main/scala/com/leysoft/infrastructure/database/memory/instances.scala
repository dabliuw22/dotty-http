package com.leysoft.infrastructure.database.memory

import cats.MonadThrow
import cats.effect.Async
import cats.syntax.applicative.*
import cats.syntax.apply.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.monadError.*
import com.leysoft.core.kernel.context.contextual.*
import com.leysoft.core.kernel.error.data.BusinessError
import com.leysoft.core.logger.Logger
import dev.profunktor.redis4cats.RedisCommands
import fs2.Stream
import io.circe.{Decoder, Encoder}
import io.circe.syntax.*
import io.circe.parser.decode
import org.typelevel.log4cats.StructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import scala.concurrent.duration.FiniteDuration

object instances:
   given [F[_]](using
     F: Async[F],
     T: MonadThrow[F],
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
          .adaptError(error => Redis.RedisError(error.getMessage))
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
            .adaptError(error => Redis.RedisError(error.getMessage))
      override def jsGet[A](using D: Decoder[A])(
        key: String
      ): Contextual[F[Option[A]]] =
        Logger[F].info(s"jsGet: $key") *> get(key).map(
          _.flatMap(json => decode[A](json).toOption)
        )
      override def jsSet[A](using
        E: Encoder[A]
      )(key: String, value: A): Contextual[F[Unit]] =
        Logger[F].info(s"jsSet: $key") *> value
          .asJson
          .noSpaces
          .pure[F]
          .flatMap(json => set(key, json))
      override def hGet(
        key: String,
        field: String
      ): Contextual[F[Option[String]]] =
        Logger[F].info(s"hGet: $key") *> R
          .hGet(key, field)
          .adaptError(error => Redis.RedisError(error.getMessage))
      override def hSet(
        key: String,
        field: String,
        value: String
      ): Contextual[F[Boolean]] =
        Logger[F].info(s"hSet: $key") *> R
          .hSet(key, field, value)
          .adaptError(error => Redis.RedisError(error.getMessage))
      override def hDel(
        key: String,
        fields: String*
      ): Contextual[F[Long]] =
        Logger[F].info(s"hDel: $key") *> R
          .hDel(key, fields.distinct*)
          .adaptError(error => Redis.RedisError(error.getMessage))
      override def get(key: String): Contextual[F[Option[String]]] =
        Logger[F].info(s"get: $key") *> R
          .get(key)
          .adaptError(error => Redis.RedisError(error.getMessage))
      override def set(
        key: String,
        value: String
      ): Contextual[F[Unit]] =
        Logger[F].info(s"set: $key") *> R
          .set(key, value)
          .adaptError(error => Redis.RedisError(error.getMessage))
      override def del(key: String): Contextual[F[Long]]           =
        Logger[F].info(s"del: $key") *> R
          .del(key)
          .adaptError(error => Redis.RedisError(error.getMessage))
      override def expire(
        key: String,
        expiration: FiniteDuration
      ): Contextual[F[Boolean]] =
        Logger[F].info(s"expire: $key") *> R
          .expire(key, expiration)
          .adaptError(error => Redis.RedisError(error.getMessage))
