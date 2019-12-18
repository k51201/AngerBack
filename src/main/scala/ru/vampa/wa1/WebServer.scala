package ru.vampa.wa1

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.staticcontent._
import org.http4s.{HttpApp, HttpRoutes}

object WebServer extends IOApp {
  val service1: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Root / "hello" / name =>
        Ok(s"Hello, $name.")
    }

  val httpApp: HttpApp[IO] = Router[IO](
    "/api" -> service1,
    "/" -> fileService(FileService.Config("D:\\Workspace\\WebApp1\\target\\scala-2.12\\classes\\public"))
  ).orNotFound

  override def run(args: List[String]): IO[ExitCode] = {
    BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve.compile.drain
      .as(ExitCode.Success)
  }
}
