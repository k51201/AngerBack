package ru.vampa.angerback

import cats.effect.{Async, Sync}
import cats.implicits._
import com.github.t3hnar.bcrypt._
import ru.vampa.angerback.db.UserRepository
import ru.vampa.angerback.db.models.UserEntity
import ru.vampa.angerback.dto.{AuthRequest, RegRequest, User}

class UserService[F[_] : Async](repo: UserRepository[F]) {
  type UserId = String

  def authentication(auth: AuthRequest): F[Either[String, UserId]] = {
    if (auth.email.isEmpty || auth.password.isEmpty) {
      Sync[F].pure(Left("Все поля должны быть заполнены!"))
    } else {
      repo.findUser(auth.email).map {
        case Some(user) if auth.password.isBcrypted(user.password) => Right(user._id.toString)
        case _ => Left("Логин и пароль неверны!")
      }
    }
  }

  def registration(reg: RegRequest): F[Either[String, UserId]] = {
    if (reg.username.isEmpty || reg.email.isEmpty || reg.password.isEmpty || reg.repeatpassword.isEmpty) {
      Sync[F].pure(Left("Все поля должны быть заполнены!"))
    } else if (reg.password != reg.repeatpassword) {
      Sync[F].pure(Left("Пароли не совпадают!"))
    } else {
      repo.findUser(reg.email).flatMap {
        case Some(_) => Sync[F].pure(Left("Имя занято!"))
        case None =>
          val hash = reg.password.bcrypt(8)
          repo.createUser(UserEntity(reg.username, reg.email, hash))
            .map(Either.right[String, UserId])
            .recover {
              case _ => Either.left[String, UserId]("Ошибка, попробуйте позже!")
            }
      }
    }
  }

  def getUser(id: String): F[Either[String, User]] = {
    repo.findUserById(id).map(_.toRight("User not found").map(e => User(e._id.toString, e.username, e.email)))
  }
}
