package com.leysoft.infrastructure.database.sql.skunk

import com.leysoft.core.kernel.context.contextual.*
import com.leysoft.core.kernel.error.data.BusinessError
import skunk.*

trait Skunk[F[_]]:
   def option[A, B](
     query: Query[B, A],
     args: B
   ): Contextual[F[Option[A]]]
   def stream[A, B](
     query: Query[B, A],
     args: B,
     size: Int = 64
   ): ContextualStream[F, A]
   def list[A, B](query: Query[B, A], args: B): Contextual[F[List[A]]]
   def command[A](command: Command[A], args: A): Contextual[F[Int]]

object Skunk:
   inline def apply[F[_]](using F: Skunk[F]): Skunk[F] =
     summon[Skunk[F]]

   case class SkunkError(message: String, code: String)
       extends BusinessError(message, code)

   object SkunkError:
      inline def apply(message: String): BusinessError =
        SkunkError(message, "00030")
