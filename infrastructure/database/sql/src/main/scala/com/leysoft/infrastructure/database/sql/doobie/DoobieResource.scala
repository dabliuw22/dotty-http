package com.leysoft.infrastructure.database.sql.doobie

import cats.effect.{Async, Resource}
import com.leysoft.core.kernel.env.FromEnv
import com.leysoft.infrastructure.database.sql.doobie.config.DoobieConfiguration
import com.leysoft.infrastructure.database.sql.doobie.instances.given
import doobie.util.transactor.Transactor
import org.typelevel.log4cats.Logger

object DoobieResource:
   inline def apply[F[_]: Async](using
     C: FromEnv[F, DoobieConfiguration],
     L: Logger[F]
   ): Resource[F, Doobie[F]] =
     for
        config                           <- C.load
        transactor @ given Transactor[F] <- config.transactor
     yield Doobie[F]
