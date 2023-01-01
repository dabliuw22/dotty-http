package com.leysoft.api

import cats.Applicative
import cats.effect.{Async, ExitCode, IO, IOApp, Resource, Spawn}
import cats.effect.std.{Console, Supervisor}
import cats.effect.std.syntax.supervisor.*
import cats.effect.syntax.spawn.*
import cats.syntax.applicative.*
import cats.syntax.eq.*
import cats.syntax.flatMap.*
import cats.syntax.show.*
import ciris.refined.*
import config.ApiConfiguration
import com.leysoft.core.kernel.env.FromEnv
import com.leysoft.core.kernel.message.Producer
import com.leysoft.core.kernel.message.data.*
import com.leysoft.infrastructure.database.memory.*
import com.leysoft.infrastructure.database.memory.config.RedisConfiguration.given
import com.leysoft.infrastructure.database.sql.skunk.*
import com.leysoft.infrastructure.database.sql.skunk.config.SkunkConfiguration.given
import com.leysoft.infrastructure.http.server.HttpServerResource
import com.leysoft.infrastructure.http.server.config.HttpServerConfiguration
import com.leysoft.infrastructure.http.server.middleware.context.*
import com.leysoft.infrastructure.message.kafka.*
import com.leysoft.infrastructure.message.kafka.config.ProducerConfiguration.given
import com.leysoft.infrastructure.message.kafka.serde.given
import fs2.kafka.*
import fs2.kafka.Deserializer.given
import fs2.kafka.Serializer.given
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.http4s.blaze.server.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Server
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger
import skunk.Session

import scala.concurrent.ExecutionContext

object Api extends IOApp:
   override def run(args: List[String]): IO[ExitCode] =
     Application[IO]
       .use(_ => IO.never)
       .as(ExitCode.Success)

object Application:
   def apply[F[_]: Async: Console: Spawn]: Resource[F, Server] =
     for
        _ @ given Supervisor[F]            <- Supervisor[F]
        _ @ given Logger[F]                <-
          Resource.eval(Slf4jLogger.create[F])
        apiConfig @ given ApiConfiguration <-
          FromEnv[F, ApiConfiguration].load
        _ @ given HttpServerConfiguration  <-
          FromEnv[F, HttpServerConfiguration].load
        // skunk @ given Skunk[F]       <- SkunkResource[F]
        // redis @ given Redis[F]       <- RedisResource[F]
        _ @ given Producer[F]              <-
          KafkaProducerResource[F](apiConfig.clientId.value)
        _ @ given ExecutionContext = ExecutionContext.global
        // action @ given Action[F]     = Action[F] // instance in com.leysoft.api.Action
        // program @ given Program[F]   = Program[F] // instance in com.leysoft.api.Program
        service                    = helloWorldService[F]
        server <- HttpServerResource[F](service)
     yield server

   private def helloWorldService[F[_]: Async](using
     P: Program[F]
   ): HttpRoutes[F] =
      val dsl = Http4sDsl[F]
      import dsl.*
      HttpRoutes
        .of[F] { case request @ GET -> Root / "hello" / name =>
          request.handle {
            P
              .run(name)
              .flatMap(_ => Ok(s"Hello, $name."))
          }
        }
