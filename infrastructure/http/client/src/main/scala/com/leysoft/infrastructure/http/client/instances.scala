package com.leysoft.infrastructure.http.client

import cats.MonadThrow
import cats.effect.{Async, Resource}
import cats.syntax.applicative.*
import cats.syntax.apply.*
import cats.syntax.flatMap.*
import cats.syntax.monadError.*
import com.leysoft.core.kernel.context.contextual.*
import com.leysoft.core.logger.Logger
import com.leysoft.infrastructure.http.client.HttpClient.*
import com.leysoft.infrastructure.http.kernel.headers.*
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.jdkhttpclient.JdkHttpClient
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.{EntityDecoder, MediaType, Request, Response, Status, Uri}
import org.http4s.Method.*
import org.typelevel.log4cats.Logger as Logger4Cats
import org.typelevel.log4cats.StructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import java.net.http.HttpClient as JavaHttpClient

object instances:
   extension (backend: HttpBackend)
     def client[F[_]](using
       F: Async[F],
       L: Logger4Cats[F]
     ): Resource[F, Client[F]] =
       backend match
          case HttpBackend.Blaze => blaze[F]
          case HttpBackend.Jdk   => jdkHttp[F]

   inline def blaze[F[_]](using
     F: Async[F],
     L: Logger4Cats[F]
   ): Resource[F, Client[F]] =
     BlazeClientBuilder[F]
       .resource
       .preAllocate(L.info("Acquire HttpClient[Blaze]..."))
       .onFinalize(L.info("Release HttpClient[Blaze]..."))

   inline def jdkHttp[F[_]](using
     F: Async[F],
     L: Logger4Cats[F]
   ): Resource[F, Client[F]] =
     JdkHttpClient
       .simple[F]
       .preAllocate(L.info("Acquire HttpClient[Jdk]..."))
       .onFinalize(L.info("Release HttpClient[Jdk]..."))

   inline def jdkHttp[F[_]](using
     F: Async[F],
     L: Logger4Cats[F]
   )(client: JavaHttpClient): Resource[F, Client[F]] =
     JdkHttpClient
       .apply(client)
       .preAllocate(L.info("Acquire HttpClient[Jdk]..."))
       .onFinalize(L.info("Release HttpClient[Jdk]..."))

   private class DefaultHttpClient[F[_]](using
     F: Async[F],
     T: MonadThrow[F],
     C: Client[F],
     L: StructuredLogger[F]
   ) extends HttpClient[F]
       with Http4sClientDsl[F]:
      override def status(uri: Uri): Kind[F, Status] =
        Logger[F].info(s"status: $uri") *> C.statusFromUri(uri)
      override def status(
        request: Request[F]
      ): Kind[F, Status] =
        Logger[F].info(
          s"status: ${request.method.name}: ${request.uri}"
        ) *> C
          .status(request.withContext)
          .adaptError(error => HttpClientError(error.getMessage))
      override def get[A](uri: Uri)(
        f: Response[F] => F[A]
      ): Kind[F, A] =
        Logger[F].info(s"get: $uri") *>
          run(GET(uri))(f)
      override def run[A](
        request: Request[F]
      )(f: Response[F] => F[A]): Kind[F, A] =
        Logger[F].info(
          s"run: ${request.method.name}: ${request.uri}"
        ) *>
          C.run(request.withContext)
            .use(f)
            .adaptError(error => HttpClientError(error.getMessage))
      override def expect[A](request: Request[F])(using
        D: EntityDecoder[F, A]
      ): Kind[F, A] = Logger[F].info(
        s"expect: ${request.method.name}: ${request.uri}"
      ) *> C
        .expect(request.withContext)
        .adaptError(error => HttpClientError(error.getMessage))
      override def expectOption[A](request: Request[F])(using
        D: EntityDecoder[F, A]
      ): Kind[F, Option[A]] =
        Logger[F].info(
          s"expectOption: ${request.method.name}: ${request.uri}"
        ) *> C
          .expectOption(request.withContext)
          .adaptError(error => HttpClientError(error.getMessage))
      override def expectOr[A](request: Request[F])(
        onError: Response[F] => F[Throwable]
      )(using D: EntityDecoder[F, A]): Kind[F, A] =
        Logger[F].info(
          s"expectOr: ${request.method.name}: ${request.uri}"
        ) *> C
          .expectOr(request.withContext.pure[F])(onError)
      override def expectOptionOr[A](request: Request[F])(
        onError: Response[F] => F[Throwable]
      )(using D: EntityDecoder[F, A]): Kind[F, Option[A]] =
        Logger[F].info(
          s"expectOptionOr: ${request.method.name}: ${request.uri}"
        ) *> C
          .expectOptionOr(request.withContext)(onError)
      override def stream(
        request: Request[F]
      ): Flow[F, Response[F]] =
        fs2
          .Stream
          .eval(
            Logger[F]
              .info(s"stream: ${request.method.name}: ${request.uri}")
          ) >> C
          .stream(request.withContext)
          .adaptError(error => HttpClientError(error.getMessage))

   given [F[_]](using
     F: Async[F],
     T: MonadThrow[F],
     C: Client[F]
   ): HttpClient[F] =
      given StructuredLogger[F] = Slf4jLogger.getLogger[F]
      DefaultHttpClient[F]
