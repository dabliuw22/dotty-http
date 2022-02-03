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
import org.typelevel.log4cats.Logger

import scala.concurrent.duration.*

object config:
   case class ProducerConfiguration(
     server: NonEmptyString,
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
               linger,
               batchSize,
               idempotence,
               request,
               retries,
               compression
             ) =>
               ProducerConfiguration(
                 server,
                 linger,
                 batchSize,
                 idempotence,
                 request,
                 retries,
                 compression
               )
           }.resource

   extension (config: ProducerConfiguration)
     def settings[F[_]: Async, K, V](clientId: String)(
       key: Serializer[F, K],
       value: Serializer[F, V]
     ): Resource[F, ProducerSettings[F, K, V]] =
       Resource.make(
         ProducerSettings(
           keySerializer = key,
           valueSerializer = value
         ).withBootstrapServers(config.server)
           .withClientId(clientId)
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

   extension [F[_]: Async, K, V](settings: ProducerSettings[F, K, V])
     def kafka(using
       L: Logger[F]
     ): Resource[F, KafkaProducer[F, K, V]] =
       KafkaProducer
         .resource[F, K, V](settings)
         .preAllocate(L.info("Acquire Producer..."))
         .onFinalize(L.info("Release Producer..."))

   case class ConsumerConfiguration(
     server: NonEmptyString,
     autoCommit: Boolean
   )

   object ConsumerConfiguration:
      given [F[_]: Async]: FromEnv[F, ConsumerConfiguration] with
         override def load: Resource[F, ConsumerConfiguration] =
           (
             env("KAFKA_SERVER").as[NonEmptyString],
             env("KAFKA_AUTO_COMMIT").as[Boolean]
           ).parMapN { (server, autoCommit) =>
             ConsumerConfiguration(server, autoCommit)
           }.resource

   extension (config: ConsumerConfiguration)
     def settings[F[_]: Async, K, V](groupId: String)(
       key: Deserializer[F, K],
       value: Deserializer[F, V]
     ): Resource[F, ConsumerSettings[F, K, V]] =
       Resource.make(
         ConsumerSettings(
           keyDeserializer = key,
           valueDeserializer = value
         ).withBootstrapServers(config.server)
           .withGroupId(groupId)
           .withEnableAutoCommit(config.autoCommit)
           .withAutoOffsetReset(AutoOffsetReset.Earliest)
           .withIsolationLevel(IsolationLevel.ReadCommitted)
           .pure[F]
       )(_ => ().pure[F])

   extension [F[_]: Async, K, V](settings: ConsumerSettings[F, K, V])
     def kafka(using
       L: Logger[F]
     ): Resource[F, KafkaConsumer[F, K, V]] =
       KafkaConsumer
         .resource[F, K, V](settings)
         .preAllocate(L.info("Acquire Consumer..."))
         .onFinalize(L.info("Release Consumer..."))
