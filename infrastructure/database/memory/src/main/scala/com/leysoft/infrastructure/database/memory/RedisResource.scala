package com.leysoft.infrastructure.database.memory

import cats.effect.{Async, Resource}
import com.leysoft.core.kernel.env.FromEnv
import com.leysoft.infrastructure.database.memory.config.RedisConfiguration
import com.leysoft.infrastructure.database.memory.instances.given
import dev.profunktor.redis4cats.RedisCommands
import org.typelevel.log4cats.Logger

object RedisResource:
   inline def apply[F[_]: Async](using
     C: FromEnv[F, RedisConfiguration],
     L: Logger[F]
   ): Resource[F, Redis[F]] =
     for
        config                                            <- C.load
        commands @ given RedisCommands[F, String, String] <-
          config.session[F]
     yield Redis[F]
