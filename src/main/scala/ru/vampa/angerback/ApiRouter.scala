package ru.vampa.angerback

import cats.effect.Async
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import ru.vampa.angerback.dto.RegRequest

class ApiRouter[F[_]: Async](service: Service[F]) extends Http4sDsl[F]{
  val routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root / "reg" => for {
        reg <- req.as[RegRequest]
        formRes <- service.registration(reg)
        res <- Ok(formRes)
      } yield res
    }
}
