package com.leysoft.infrastructure.database.sql.skunk

import cats.effect.{Async, Resource}
import cats.effect.std.Console
import com.leysoft.core.kernel.env.FromEnv
import com.leysoft.infrastructure.database.sql.skunk.instances.given
import com.leysoft.infrastructure.database.sql.skunk.config.SkunkConfiguration
import org.typelevel.log4cats.Logger
import skunk.Session

object SkunkResource:
   inline def apply[F[_]: Async](using
     C: FromEnv[F, SkunkConfiguration],
     L: Logger[F],
     S: Console[F]
   ): Resource[F, Skunk[F]] =
     for
        config                        <- C.load
        transactor @ given Session[F] <- config.pool[F]
     yield Skunk[F]
