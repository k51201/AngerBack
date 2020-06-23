package ru.vampa.angerback

import java.time.Clock

import cats.data.{Kleisli, OptionT}
import cats.effect.Async
import cats.implicits._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware
import org.reactormonk.{CryptoBits, PrivateKey}
import ru.vampa.angerback.dto.{AuthRequest, FormResponse, RegRequest, User}
import ru.vampa.angerback.services.{DialogService, UserService}

class ApiRouter[F[_]: Async](
    userService: UserService[F],
    dialogService: DialogService[F]
) extends Http4sDsl[F] {
  val key: PrivateKey = PrivateKey(scala.io.Codec.toUTF8("rgghnfghndzs"))
  val crypto: CryptoBits = CryptoBits(key)
  val clock: Clock = java.time.Clock.systemUTC

  val authUser: Kleisli[F, Request[F], Either[String, User]] =
    Kleisli { req =>
      val message = for {
        header <- headers.Cookie.from(req.headers).toRight("Cookie parsing error")
        cookie <- header.values.toList.find(_.name == "sid").toRight("Couldn't find the session id")
        message <- crypto.validateSignedToken(cookie.content).toRight("Cookie invalid")
      } yield message
      message.flatTraverse(userService.getUser)
    }
  val onFailure: AuthedRoutes[String, F] = Kleisli { req =>
    OptionT.liftF(Forbidden(req.context))
  }
  val authMiddle: AuthMiddleware[F, User] = AuthMiddleware(authUser, onFailure)

  val routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root / "auth"        => auth(req)
      case req @ POST -> Root / "reg"         => register(req)
      case GET -> Root / "logout"             => Ok().map(_.removeCookie("sid"))
      case GET -> Root / "users" :? search(s) => searchUsers(s)
    } <+> authMiddle(AuthedRoutes.of[User, F] {
      case GET -> Root / "currentuser" as user => Ok(user)
      case POST -> Root / "dialogs" :? userId(withId) as user =>
        createDialog(user, withId)
      case GET -> Root / "dialogs" as user => userDialogs(user)
    })

  private def userDialogs(user: User): F[Response[F]] = {
    dialogService.userDialogs(user.id).flatMap {
      case Left(err) => InternalServerError(err)
      case Right(ds) => Ok(ds)
    }
  }

  private def createDialog(user: User, withId: String): F[Response[F]] = {
    dialogService.createDialog(user.id, withId).flatMap {
      case Left(err) => InternalServerError(err)
      case Right(id) => Ok(id)
    }
  }

  private def searchUsers(query: Option[String]): F[Response[F]] = {
    userService.searchUsers(query).flatMap {
      case Left(err)    => InternalServerError(err)
      case Right(users) => Ok(users)
    }
  }

  private def auth(req: Request[F]): F[Response[F]] = {
    for {
      auth <- req.as[AuthRequest]
      authRes <- userService.authentication(auth)
      res <- authRes match {
        case Left(msg) => Ok(FormResponse.error(msg))
        case Right(userId) =>
          val sid = crypto.signToken(userId, clock.millis.toString)
          Ok(FormResponse.success).map(_.addCookie(ResponseCookie("sid", sid)))
      }
    } yield res
  }

  private def register(req: Request[F]): F[Response[F]] = {
    for {
      reg <- req.as[RegRequest]
      regRes <- userService.registration(reg)
      res <- regRes match {
        case Left(msg) => Ok(FormResponse.error(msg))
        case Right(_)  => Ok(FormResponse.success)
      }
    } yield res
  }

  object search extends OptionalQueryParamDecoderMatcher[String]("search")
  object userId extends QueryParamDecoderMatcher[String]("userId")
}
