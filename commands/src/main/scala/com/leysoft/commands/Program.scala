package com.leysoft.commands

import cats.effect.Async
import cats.effect.std.Supervisor
import cats.syntax.apply.*
import cats.syntax.applicative.*
import cats.syntax.flatMap.*
import com.leysoft.core.kernel.context.contextual.*
import com.leysoft.core.logger.Logger
import org.typelevel.log4cats.StructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait Program[F[_]]:
   def run(name: String): Kind[F, Unit]

object Program:
   inline def apply[F[_]](using
     F: Program[F]
   ): Program[F] = F

   given [F[_]: Async](using
     S: Supervisor[F],
     C: ContextHandler[F],
     A: Action[F]
   ): Program[F] with
      given StructuredLogger[F] = Slf4jLogger.getLogger[F]
      override def run(name: String): Kind[F, Unit] =
        C.kind
          .flatMap { ctx =>
            S.supervise(Logger[F].info(s"Init: $name, $ctx")) *>
              A.run(name) *>
              S.supervise(Logger[F].info(s"End $name, $ctx")) *> ()
                .pure[F]
          }
