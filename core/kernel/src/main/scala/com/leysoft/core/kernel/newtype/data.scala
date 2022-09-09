package com.leysoft.core.kernel.newtype

import cats.kernel.Eq
import cats.Show
import cats.kernel.Order
import io.circe.{Decoder, Encoder}
import monocle.Iso

object data:
   trait Wrapper[A, B]:
      def iso: Iso[A, B]
   object Wrapper:
      def apply[A, B](using
        ev: Wrapper[A, B]
      ): Wrapper[A, B] = ev

   abstract class Newtype[A](using
     eqv: Eq[A],
     ord: Order[A],
     shw: Show[A],
     enc: Encoder[A],
     dec: Decoder[A]
   ):
      opaque type Type = A

      inline def apply(a: A): Type = a

      protected inline final def derive[F[_]](using
        ev: F[A]
      ): F[Type] = ev

      extension (t: Type) inline def value: A                = t
      extension (t: Type) inline def replace(v: A): Type     = v
      extension (t: Type) inline def modify(f: A => A): Type = f(t)

      given Wrapper[A, Type] with
         def iso: Iso[A, Type] =
           Iso[A, Type](apply(_))(_.value)

      given Eq[Type]       = eqv
      given Order[Type]    = ord
      given Show[Type]     = shw
      given Encoder[A]     = enc
      given Decoder[A]     = dec
      given Ordering[Type] = ord.toOrdering
