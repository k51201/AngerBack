package ru.vampa.angerback

import java.time.Clock

import cats.effect.Async
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Request, Response, ResponseCookie}
import org.reactormonk.{CryptoBits, PrivateKey}
import ru.vampa.angerback.dto.{AuthRequest, FormResponse, RegRequest}

class ApiRouter[F[_]: Async](service: Service[F]) extends Http4sDsl[F] {
  val key: PrivateKey = PrivateKey(scala.io.Codec.toUTF8(scala.util.Random.alphanumeric.take(20).mkString("")))
  val crypto: CryptoBits = CryptoBits(key)
  val clock: Clock = java.time.Clock.systemUTC

  val routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root / "auth" => authRoute(req)
      case req @ POST -> Root / "reg"  => regRoute(req)
    }

  private def authRoute(req: Request[F]): F[Response[F]] = {
    for {
      auth <- req.as[AuthRequest]
      authRes <- service.authentication(auth)
      res <- authRes match {
        case Left(msg) => Ok(FormResponse.error(msg))
        case Right(userId) =>
          val sid = crypto.signToken(userId, clock.millis.toString)
          Ok(FormResponse.success).map(_.addCookie(ResponseCookie("sid", sid)))
      }
    } yield res
  }

  private def regRoute(req: Request[F]): F[Response[F]] = {
    for {
      reg <- req.as[RegRequest]
      regRes <- service.registration(reg)
      res <- regRes match {
        case Left(msg) => Ok(FormResponse.error(msg))
        case Right(_) => Ok(FormResponse.success)
      }
    } yield res
  }
}
