package com.leysoft.infrastructure.http.client

import cats.Applicative
import cats.syntax.applicative.*
import io.circe.syntax.*
import io.circe.generic.auto.*
import org.http4s.circe.*
import org.http4s.{Method, Request, Response, Status, Uri}
import org.scalacheck.Gen
import fs2.Stream

object data:
   case class Data(value: String):
      def toResponse[F[_]](using
        F: Applicative[F]
      )(status: Status): Response[F] = Response(
        status = status,
        body = Stream
          .emits(this.asJson.noSpaces.getBytes)
          .covary[F]
      )
      def toRequest[F[_]](method: Method, uri: Uri): Request[F] =
        Request[F](method, uri).withEntity(this.asJson)

   object Data:
      inline def make[F[_]](using F: Applicative[F])(
        value: String
      ): F[Data] =
        Data(value).pure[F]

      def gen: Gen[Data] = Gen.alphaStr.map(Data(_))
