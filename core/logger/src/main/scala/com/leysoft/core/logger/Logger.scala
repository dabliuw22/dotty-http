package com.leysoft.core.logger

import cats.effect.Sync
import cats.syntax.applicative.*
import cats.syntax.flatMap.*
import cats.syntax.show.*
import com.leysoft.core.kernel.context.contextual.*
import com.leysoft.core.kernel.context.data.Context
import com.leysoft.core.kernel.newtype.instances.given
import com.leysoft.core.logger.algebra.Logger
import org.typelevel.log4cats.StructuredLogger

trait Logger[F[_]]:
   def info(message: String): Kind[F, Unit]
   def error(message: String): Kind[F, Unit]
   def error(message: String)(
     error: Throwable
   ): Kind[F, Unit]
   def debug(message: String): Kind[F, Unit]
   def warn(message: String): Kind[F, Unit]
   def trace(message: String): Kind[F, Unit]

object Logger:
   inline def apply[F[_]](using F: Logger[F]): Logger[F] = F

   extension (ctx: Context)
     inline def toMap: Map[String, String] =
       Map(
         "correlation-id" -> ctx.id.value.show
       )

   given ctxLogger[F[_]: Sync: StructuredLogger]: Logger[F] with
      override def info(message: String): Kind[F, Unit]  =
        get
          .flatMap(context =>
            StructuredLogger[F].info(context)(message)
          )
      override def error(message: String): Kind[F, Unit] =
        get
          .flatMap(context =>
            StructuredLogger[F].error(context)(message)
          )
      override def error(
        message: String
      )(error: Throwable): Kind[F, Unit] =
        get
          .flatMap(context =>
            StructuredLogger[F].error(context, error)(message)
          )
      override def debug(message: String): Kind[F, Unit] =
        get
          .flatMap(context =>
            StructuredLogger[F].debug(context)(message)
          )
      override def warn(message: String): Kind[F, Unit]  =
        get
          .flatMap(context =>
            StructuredLogger[F].warn(context)(message)
          )
      override def trace(message: String): Kind[F, Unit] =
        get
          .flatMap(context =>
            StructuredLogger[F].trace(context)(message)
          )

      private def get: Kind[F, Map[String, String]] =
        summon[Context].toMap.pure[F]
