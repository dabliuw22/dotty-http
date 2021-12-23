package com.leysoft.core.kernel.env

import cats.effect.Resource

trait FromEnv[F[_], A]:
   def load: Resource[F, A]

object FromEnv:
   inline def apply[F[_], A](using F: FromEnv[F, A]): FromEnv[F, A] =
     summon[FromEnv[F, A]]
