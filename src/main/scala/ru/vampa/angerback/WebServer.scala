package ru.vampa.angerback

import cats.effect._
import org.http4s.HttpApp
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import ru.vampa.angerback.db.UserRepository

import scala.concurrent.ExecutionContext.global

object WebServer extends IOApp {
  val repo = new UserRepository[IO]
  val service: Service[IO] = new Service[IO](repo)

  val httpApp: HttpApp[IO] = Router(
    "/api" -> new ApiRouter[IO](service).routes
  ).orNotFound

  override def run(args: List[String]): IO[ExitCode] = {
    BlazeServerBuilder[IO](global)
      .bindHttp(3001, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve.compile.drain
      .as(ExitCode.Success)
  }
}
