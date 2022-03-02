package com.leysoft.core.kernel.context

import cats.Applicative
import cats.syntax.applicative.*
import com.leysoft.core.kernel.context.data.Context

object contextual:
   // type Kind = [F[_], A] =>> Context ?=> F[A]
   type Kind[F[_], A] = Context ?=> F[A]
   object Kind:
      inline def apply[F[_]](using
        F: Applicative[F]
      ): Kind[F, Context] =
        summon[Context].pure[F]

   type Flow = [F[_], O] =>> Context ?=> fs2.Stream[F, O]
   object Flow:
      inline def apply[F[_]: Applicative]: Flow[F, Context] =
        fs2
          .Stream
          .emit(summon[Context])
          .covary[F]

   trait ContextHandler[F[_]]:
      def handle(using ctx: Context): Kind[F, Context]
      def kind: Kind[F, Context]
      def flow: Flow[F, Context]

   object ContextHandler:
      inline def apply[F[_]](using
        F: ContextHandler[F]
      ): ContextHandler[F] = F

      given [F[_]: Applicative]: ContextHandler[F] with
         override def handle(using
           ctx: Context
         ): Kind[F, Context] =
           Kind[F]
         override def kind: Kind[F, Context] =
           summon[Context].pure[F]
         override def flow: Flow[F, Context] =
           Flow[F]
