package com.leysoft.commands

import cats.effect.{Async, Resource}
import cats.syntax.parallel.*
import ciris.*
import ciris.refined.*
import com.leysoft.core.kernel.env.FromEnv
import eu.timepit.refined.types.string.NonEmptyString

object config:
   case class CommandsConfiguration(
     channel: NonEmptyString,
     clientId: NonEmptyString,
     consumerId: NonEmptyString
   )

   object CommandsConfiguration:
      given [F[_]: Async]: FromEnv[F, CommandsConfiguration] with
         override def load: Resource[F, CommandsConfiguration] =
           (
             env("COMMANDS_CHANNEL")
               .as[NonEmptyString],
             env("COMMANDS_CLIENT_ID")
               .as[NonEmptyString],
             env("COMMANDS_GROUP_ID")
               .as[NonEmptyString]
           )
             .parMapN((channel, clientId, consumerId) =>
               CommandsConfiguration(channel, clientId, consumerId)
             )
             .resource
