package com.leysoft.infrastructure.message.kafka

import cats.effect.{Async, Resource}
import com.leysoft.core.kernel.env.FromEnv
import com.leysoft.core.kernel.message.Producer
import com.leysoft.core.kernel.message.data.Message
import com.leysoft.infrastructure.message.kafka.config.*
import com.leysoft.infrastructure.message.kafka.producer.given
import fs2.kafka.*
import org.typelevel.log4cats.Logger

object KafkaProducerResource:
   inline def apply[F[_]: Async](using
     C: FromEnv[F, ProducerConfiguration],
     L: Logger[F],
     K: Serializer[F, String],
     V: Serializer[F, Message]
   )(clientId: String): Resource[F, Producer[F]] =
     for
        config <- C.load
        settings @ given ProducerSettings[F, String, Message] <-
          config.settings[F, String, Message](clientId)(K, V)
        producer @ given KafkaProducer[F, String, Message]    <-
          settings.kafka
     yield Producer[F]
