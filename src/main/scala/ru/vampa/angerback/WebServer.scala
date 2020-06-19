package ru.vampa.angerback

import cats.effect._
import org.http4s.dsl.io._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import org.http4s.{HttpApp, HttpRoutes}

import scala.concurrent.ExecutionContext.global

object WebServer extends IOApp {
  val apiRouter: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Root / "hello" / name =>
        Ok(s"Hello, $name.")
    }

  val httpApp: HttpApp[IO] = Router(
    "/api" -> apiRouter
  ).orNotFound

  override def run(args: List[String]): IO[ExitCode] = {
    BlazeServerBuilder[IO](global)
      .bindHttp(3001, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve.compile.drain
      .as(ExitCode.Success)
  }
}
