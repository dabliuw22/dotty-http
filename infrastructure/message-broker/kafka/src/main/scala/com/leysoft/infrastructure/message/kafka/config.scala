package com.leysoft.infrastructure.message.kafka

import cats.effect.{Async, Concurrent, Resource}
import cats.effect.std.Console
import cats.syntax.applicative.*
import cats.syntax.parallel.*
import ciris.*
import ciris.refined.*
import com.leysoft.core.kernel.env.FromEnv
import com.leysoft.core.kernel.message.data.Message
import eu.timepit.refined.auto.*
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.string.NonEmptyString
import fs2.kafka.*
import org.apache.kafka.clients.producer.ProducerConfig
import scala.concurrent.duration.*
import serde.*

object config:
   case class ProducerConfiguration(
     server: NonEmptyString,
     clientId: NonEmptyString,
     linger: FiniteDuration,
     batchSize: PosInt,
     idempotence: Boolean,
     requestsPerConnection: PosInt,
     retries: PosInt,
     compression: NonEmptyString
   )

   object ProducerConfiguration:
      given [F[_]: Async]: FromEnv[F, ProducerConfiguration] with
         override def load: Resource[F, ProducerConfiguration] =
           (
             env("KAFKA_SERVER").as[NonEmptyString],
             env("KAFKA_CLIENT_ID").as[NonEmptyString],
             env("KAFKA_LINGER_MS")
               .as[PosInt]
               .map(millis =>
                 FiniteDuration.apply(millis.value, MILLISECONDS)
               ),
             env("KAFKA_BATCH_SIZE").as[PosInt],
             env("KAFKA_IDEMPOTENCE").as[Boolean],
             env("KAFKA_REQUEST_PER_CONNECTION")
               .as[PosInt],
             env("KAFKA_RETRIES").as[PosInt],
             env("KAFKA_COMPRESSION").as[NonEmptyString]
           ).parMapN {
             (
               server,
               clientId,
               linger,
               batchSize,
               idempotence,
               request,
               retries,
               compression
             ) =>
               ProducerConfiguration(
                 server,
                 clientId,
                 linger,
                 batchSize,
                 idempotence,
                 request,
                 retries,
                 compression
               )
           }.resource

   extension (config: ProducerConfiguration)
     def settings[F[_]: Async]
       : Resource[F, ProducerSettings[F, String, Message]] =
       Resource.make(
         ProducerSettings(
           keySerializer = keySerializer,
           valueSerializer = valueSerializer
         ).withBootstrapServers(config.server)
           .withClientId(config.clientId)
           .withBatchSize(config.batchSize)
           .withLinger(config.linger)
           .withEnableIdempotence(config.idempotence)
           .withMaxInFlightRequestsPerConnection(
             config.requestsPerConnection
           )
           .withRetries(config.retries)
           .withAcks(Acks.All)
           .withProperties(
             (
               ProducerConfig.COMPRESSION_TYPE_CONFIG,
               config.compression.value
             )
           )
           .pure[F]
       )(_ => ().pure[F])

   case class ConsumerConfiguration(
     server: NonEmptyString,
     groupId: NonEmptyString,
     autoCommit: Boolean
   )

   object ConsumerConfiguration:
      given [F[_]: Async]: FromEnv[F, ConsumerConfiguration] with
         override def load: Resource[F, ConsumerConfiguration] =
           (
             env("KAFKA_SERVER").as[NonEmptyString],
             env("KAFKA_GROUP_ID").as[NonEmptyString],
             env("KAFKA_AUTO_COMMIT").as[Boolean]
           ).parMapN { (server, groupId, autoCommit) =>
             ConsumerConfiguration(server, groupId, autoCommit)
           }.resource

   extension (config: ConsumerConfiguration)
     def settings[F[_]: Async]
       : Resource[F, ConsumerSettings[F, String, Message]] =
       Resource.make(
         ConsumerSettings(
           keyDeserializer = keyDeserializer,
           valueDeserializer = valueDeserializer
         ).withBootstrapServers(config.server)
           .withGroupId(config.groupId)
           .withEnableAutoCommit(config.autoCommit)
           .withAutoOffsetReset(AutoOffsetReset.Earliest)
           .withIsolationLevel(IsolationLevel.ReadCommitted)
           .pure[F]
       )(_ => ().pure[F])
