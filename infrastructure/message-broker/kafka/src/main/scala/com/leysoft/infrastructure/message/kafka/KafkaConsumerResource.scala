package com.leysoft.infrastructure.message.kafka

import cats.effect.{Async, Resource}
import com.leysoft.core.kernel.env.FromEnv
import com.leysoft.core.kernel.message.Consumer
import com.leysoft.core.kernel.message.data.Message
import com.leysoft.infrastructure.message.kafka.config.*
import com.leysoft.infrastructure.message.kafka.consumer.{ConsumerState, given}
import fs2.kafka.{ConsumerSettings, Deserializer, KafkaConsumer}
import org.typelevel.log4cats.Logger

object KafkaConsumerResource:
   inline def apply[F[_]: Async](using
     C: FromEnv[F, ConsumerConfiguration],
     L: Logger[F],
     K: Deserializer[F, String],
     V: Deserializer[F, Message],
     S: ConsumerState[F]
   )(groupId: String): Resource[F, Consumer[F]] =
     for
        config <- C.load
        settings @ given ConsumerSettings[F, String, Message] <-
          config.settings[F, String, Message](groupId)(K, V)
        producer @ given KafkaConsumer[F, String, Message]    <-
          settings.kafka
     yield Consumer[F]
