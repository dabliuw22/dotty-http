package com.leysoft.api

import cats.Applicative
import cats.effect.Async
import cats.effect.std.Supervisor
import cats.syntax.apply.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import com.leysoft.core.kernel.context.contextual.*
import com.leysoft.core.kernel.message.Producer
import com.leysoft.core.logger.Logger
import org.typelevel.log4cats.StructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait Action[F[_]]:
   def run(name: String): Kind[F, Unit]

object Action:
   inline def apply[F[_]](using F: Action[F]): Action[F] =
     summon[Action[F]]

   inline given [F[_]: Async](using
     S: Supervisor[F],
     C: ContextHandler[F],
     P: Producer[F]
   ): Action[F] with
      given StructuredLogger[F] = Slf4jLogger.getLogger[F]
      override def run(name: String): Kind[F, Unit] =
        C.kind
          .flatMap { ctx =>
            S.supervise(Logger[F].info(s"Action...$ctx"))
          }
          .void
