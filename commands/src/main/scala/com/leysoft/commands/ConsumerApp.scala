package com.leysoft.commands

import cats.effect.std.{Console, Supervisor}
import cats.effect.{Async, ExitCode, IO, IOApp, Ref, Resource, Spawn}
import cats.syntax.applicative.*
import cats.syntax.flatMap.*
import ciris.*
import config.CommandsConfiguration
import com.leysoft.core.kernel.context
import com.leysoft.core.kernel.context.data.*
import com.leysoft.core.kernel.env.FromEnv
import com.leysoft.core.kernel.message.*
import com.leysoft.core.kernel.message.data.MessageChannel.*
import com.leysoft.infrastructure.database.memory.*
import com.leysoft.infrastructure.database.memory.config.RedisConfiguration.given
import com.leysoft.infrastructure.database.sql.skunk.*
import com.leysoft.infrastructure.database.sql.skunk.config.SkunkConfiguration.given
import com.leysoft.infrastructure.message.kafka.*
import com.leysoft.infrastructure.message.kafka.config.ProducerConfiguration.given
import com.leysoft.infrastructure.message.kafka.consumer.{ConsumerState, given}
import com.leysoft.infrastructure.message.kafka.producer.given
import com.leysoft.infrastructure.message.kafka.serde.given
import fs2.kafka.*
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger
import skunk.Session

import java.util.UUID

object ConsumerApp extends IOApp:
   override def run(args: List[String]): IO[ExitCode] =
     Application[IO].use(
       _.execute("commands.channel".toChannel)
         .compile
         .drain
         .as(ExitCode.Success)
     )

object Application:
   def apply[F[_]: Async]: Resource[F, Consumer[F]] =
     for
        supervisor @ given Supervisor[F]             <- Supervisor[F]
        logger @ given Logger[F]                     <-
          Resource.eval(Slf4jLogger.create[F])
        commandsConfig @ given CommandsConfiguration <-
          FromEnv[F, CommandsConfiguration].load
        producer @ given Producer[F]                 <-
          KafkaProducerResource[F](commandsConfig.clientId.value)
        state @ given ConsumerState[F]               <- register[F]
        consumer                                     <-
          KafkaConsumerResource[F](commandsConfig.consumerId.value)
        ctx @ given Context = Context.make
        _ <- Resource.eval(
               producer.execute(
                 TestCommandTwo.make(using Context.make)("Ha")
               )
             )
        _ <- Resource.eval(producer.execute(TestCommand.make("Kame")))
     yield consumer

   def register[F[_]: Async](using
     S: Supervisor[F],
     L: Logger[F],
     P: Producer[F]
   ): Resource[F, ConsumerState[F]] =
     for
        state @ given ConsumerState.State[F] <- ConsumerState.empty[F]
        consumerState = ConsumerState[F]
        _ <-
          consumerState.register(classOf[TestCommand])(TestHandler[F])
     yield consumerState
