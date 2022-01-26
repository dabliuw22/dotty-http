package com.leysoft.infrastructure.database.memory

import cats.effect.Async
import cats.effect.Resource
import cats.syntax.functor.*
import ciris.*
import ciris.refined.*
import com.leysoft.core.kernel.env.FromEnv
import dev.profunktor.redis4cats.*
import dev.profunktor.redis4cats.connection.*
import dev.profunktor.redis4cats.RedisCommands
import dev.profunktor.redis4cats.connection.{RedisClient, RedisURI}
import dev.profunktor.redis4cats.data.RedisCodec
import dev.profunktor.redis4cats.effect.Log.NoOp.*
import dev.profunktor.redis4cats.Redis as InfraRedis
import eu.timepit.refined.types.string.NonEmptyString
import org.typelevel.log4cats.Logger

object config:
   enum Codec:
      case Utf8

   case class RedisConfiguration(
     server: NonEmptyString,
     codec: Codec = Codec.Utf8
   )

   object RedisConfiguration:
      given [F[_]: Async]: FromEnv[F, RedisConfiguration] with
         override def load: Resource[F, RedisConfiguration] =
           env("REDIS_SERVER")
             .as[NonEmptyString]
             .map(server => RedisConfiguration(server))
             .resource

   extension (config: RedisConfiguration)
      def stringCodec: RedisCodec[String, String] =
        config.codec match
           case Codec.Utf8 => RedisCodec.Utf8

      def session[F[_]: Async](using
        L: Logger[F]
      ): Resource[F, RedisCommands[F, String, String]] =
        for
           uriRedis <-
             Resource.eval(RedisURI.make[F](config.server.toString))
           client   <- RedisClient[F].fromUri(uriRedis)
           codec = config.stringCodec
           cmd <- InfraRedis[F]
                    .fromClient(client, codec)
                    .preAllocate(L.info("Acquire RedisCommands..."))
                    .onFinalize(L.info("Release RedisCommands..."))
        yield cmd
