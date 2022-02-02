package com.leysoft.infrastructure.http.client

import cats.effect.IO
import cats.syntax.eq.*
import com.leysoft.core.kernel.context.data.Context
import com.leysoft.core.kernel.context.ContextGenerator
import com.leysoft.infrastructure.http.client.*
import com.leysoft.infrastructure.http.client.data.Data
import com.leysoft.infrastructure.http.client.instances.given
import io.circe.syntax.*
import io.circe.generic.auto.*
import fs2.Stream
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.client.Client
import org.http4s.client.dsl.io.*
import org.http4s.Method.*
import org.http4s.syntax.all.*
import weaver.*

object HttpClientTest extends SimpleIOSuite:
   lazy val url  = uri"https:///test.com/test"
   given Context = ContextGenerator.simple

   test("HttpClient.status(uri) should return http status") {
     for
        client @ given Client[IO] <-
          ClientTest.make[IO](Response(status = Status.Ok))
        httpClient = HttpClient[IO]
        status <- httpClient.status(url)
     yield expect(status.equals(Status.Ok))
   }

   test("HttpClient.status(request) should return http status") {
     for
        client @ given Client[IO] <-
          ClientTest.make[IO](Response(status = Status.Ok))
        httpClient = HttpClient[IO]
        status <- httpClient.status(GET(url))
     yield expect(status.equals(Status.Ok))
   }

   test(
     "HttpClient.get should apply the transformation function of the response"
   ) {
     Data.gen.sample.fold(IO(expect(false))) { data =>
       for
          client @ given Client[IO] <-
            ClientTest.make[IO](data.toResponse[IO](Status.Ok))
          httpClient = HttpClient[IO]
          response <-
            httpClient.get[Data](url)(response =>
              if response.status.equals(Status.Ok) then
                 response.asJsonDecode[Data]
              else IO.raiseError(HttpClient.HttpClientError("Error"))
            )
          json = response.asJson.noSpaces
       yield expect(json == data.asJson.noSpaces)
     }
   }

   test(
     "HttpClient.run should apply the transformation function of the response"
   ) {
     for
        data <- Data.make("test")
        expected = data.asJson.noSpaces
        client @ given Client[IO] <-
          ClientTest.make[IO](data.toResponse[IO](Status.Ok))
        httpClient = HttpClient[IO]
        response <-
          httpClient.run[Data](data.toRequest(Method.POST, url))(
            response =>
              if response.status.equals(Status.Ok) then
                 response.asJsonDecode[Data]
              else IO.raiseError(HttpClient.HttpClientError("Error"))
          )
        json = response.asJson.noSpaces
     yield expect(json == expected)
   }

   test(
     "HttpClient.expect should transform the response"
   ) {
     for
        data <- Data.make("test")
        expected = data.asJson.noSpaces
        client @ given Client[IO] <-
          ClientTest.make[IO](data.toResponse[IO](Status.Ok))
        httpClient = HttpClient[IO]
        response <-
          httpClient.expect[Data](data.toRequest(Method.POST, url))
        json = response.asJson.noSpaces
     yield expect(json == expected)
   }

   test(
     "HttpClient.expectOption should transform the response"
   ) {
     for
        data <- Data.make("test")
        expected = data.asJson.noSpaces
        client @ given Client[IO] <-
          ClientTest.make[IO](data.toResponse[IO](Status.Ok))
        httpClient = HttpClient[IO]
        response <-
          httpClient.expectOption[Data](
            data.toRequest(Method.POST, url)
          )
        json = response.map(_.asJson.noSpaces)
     yield expect(json.contains(expected))
   }

   test(
     "HttpClient.expectOr should transform the response"
   ) {
     for
        data <- Data.make("test")
        expected = data.asJson.noSpaces
        client @ given Client[IO] <-
          ClientTest.make[IO](data.toResponse[IO](Status.Ok))
        httpClient = HttpClient[IO]
        response <-
          httpClient.expectOr[Data](
            data.toRequest(Method.POST, url)
          )(response =>
            IO.raiseError(HttpClient.HttpClientError("Error"))
          )
        json = response.asJson.noSpaces
     yield expect(json == expected)
   }

   test(
     "HttpClient.expectOptionOr should transform the response"
   ) {
     for
        data <- Data.make("test")
        expected = data.asJson.noSpaces
        client @ given Client[IO] <-
          ClientTest.make[IO](data.toResponse[IO](Status.Ok))
        httpClient = HttpClient[IO]
        response <-
          httpClient.expectOptionOr[Data](
            data.toRequest(Method.POST, url)
          )(response =>
            IO.raiseError(HttpClient.HttpClientError("Error"))
          )
        json = response.map(_.asJson.noSpaces)
     yield expect(json.contains(expected))
   }
