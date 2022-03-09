package com.leysoft.infrastructure.database.sql.doobie

import com.leysoft.core.kernel.context.contextual.*
import com.leysoft.core.kernel.error.data.BusinessError
import doobie.ConnectionIO
import doobie.util.query.Query0
import doobie.util.update.Update0

trait Doobie[F[_]]:
   def option[T](query: Query0[T]): Kind[F, Option[T]]
   def stream[T](query: Query0[T]): Flow[F, T]
   def list[T](query: Query0[T]): Kind[F, List[T]]
   def command(command: Update0): Kind[F, Int]
   def transaction[A](program: => ConnectionIO[A]): Kind[F, A]

object Doobie:
   inline def apply[F[_]](using F: Doobie[F]): Doobie[F] =
     summon[Doobie[F]]

   case class DoobieError(message: String, code: String)
       extends BusinessError(message, code)

   object DoobieError:
      inline def apply(message: String): BusinessError =
        DoobieError(message, "00020")
