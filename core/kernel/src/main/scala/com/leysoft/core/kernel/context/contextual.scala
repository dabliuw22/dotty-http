package com.leysoft.core.kernel.context

import cats.Applicative
import cats.syntax.applicative.*
import com.leysoft.core.kernel.context.data.Context
import fs2.Stream

object contextual:
   // type Contextual = [F] =>> Context ?=> F
  
   type Contextual[F] = Context ?=> F
   object Contextual:
      inline def apply[F[_]](using
        F: Applicative[F]
      ): Contextual[F[Context]] =
        summon[Context].pure[F]

   type ContextualStream = [F[_], O] =>> Context ?=> Stream[F, O]
   object ContextualStream:
      inline def apply[F[_]: Applicative]
        : ContextualStream[F, Context] =
        Stream.eval(summon[Context].pure[F])

   trait ContextHandler[F[_]]:
      def handle(ctx: Context): Contextual[F[Context]]
      def get: Contextual[F[Context]]
      def getS: ContextualStream[F, Context]

   object ContextHandler:
      inline def apply[F[_]](using
        F: ContextHandler[F]
      ): ContextHandler[F] = F

      given [F[_]: Applicative]: ContextHandler[F] with
         override def handle(using
           ctx: Context
         ): Contextual[F[Context]] =
           Contextual[F]
         override def get: Contextual[F[Context]]        =
           summon[Context].pure[F]
         override def getS: ContextualStream[F, Context] =
           ContextualStream[F]
