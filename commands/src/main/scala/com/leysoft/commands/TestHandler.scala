package com.leysoft.commands

import cats.effect.Async
import com.leysoft.core.kernel.context.contextual.*
import com.leysoft.core.kernel.message.{Handler, data}
import com.leysoft.core.logger.Logger
import org.typelevel.log4cats.StructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import fs2.Stream

final class TestHandler[F[_]: Async: ContextHandler]
    extends Handler[F]:
   given StructuredLogger[F] = Slf4jLogger.getLogger[F]
   override def execute[A <: data.Message](
     message: A
   ): Flow[F, data.Message] =
     message match
        case TestCommand(_, _, _, name) =>
          ContextHandler[F]
            .flow
            .flatMap { ctx =>
              Stream.eval(Logger[F].info(s"$name, $ctx"))
            }
            .as(message)
        case _                          => Stream.emit(message)
