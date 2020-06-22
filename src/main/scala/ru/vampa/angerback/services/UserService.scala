package ru.vampa.angerback.services

import cats.effect.Sync
import cats.implicits._
import com.github.t3hnar.bcrypt._
import ru.vampa.angerback.db.UserRepository
import ru.vampa.angerback.db.models.UserEntity
import ru.vampa.angerback.dto.{AuthRequest, RegRequest, User}

class UserService[F[_] : Sync](repo: UserRepository[F]) {
  type UserId = String

  def authentication(auth: AuthRequest): F[Either[String, UserId]] = {
    if (auth.email.isEmpty || auth.password.isEmpty) {
      Sync[F].pure(Left("Все поля должны быть заполнены!"))
    } else {
      repo.findOne(auth.email).map {
        case Some(user) if auth.password.isBcrypted(user.password) => Right(user.id.toString)
        case _ => Left("Логин и пароль неверны!")
      }.recover {
        case e => Either.left[String, UserId](e.getMessage)
      }
    }
  }

  def registration(reg: RegRequest): F[Either[String, UserId]] = {
    if (reg.username.isEmpty || reg.email.isEmpty || reg.password.isEmpty || reg.repeatpassword.isEmpty) {
      Sync[F].pure(Left("Все поля должны быть заполнены!"))
    } else if (reg.password != reg.repeatpassword) {
      Sync[F].pure(Left("Пароли не совпадают!"))
    } else {
      repo.findOne(reg.email).flatMap {
        case Some(_) => Sync[F].pure(Left("Имя занято!"))
        case None =>
          val hash = reg.password.bcrypt(8)
          repo.create(UserEntity(reg.username, reg.email, hash))
            .map(Either.right[String, UserId])
            .recover {
              case _ => Either.left[String, UserId]("Ошибка, попробуйте позже!")
            }
      }
    }
  }

  def getUser(id: String): F[Either[String, User]] = {
    repo.findById(id)
      .map(_.toRight("User not found").map(User.apply))
      .recover {
        case e => Either.left[String, User](e.getMessage)
      }
  }

  def searchUsers(query: Option[String]): F[Either[String, Seq[User]]] = {
    query.fold(repo.findAll())(repo.find)
      .map(_.map(User.apply).asRight[String])
      .recover {
        case e => Either.left[String, Seq[User]](e.getMessage)
      }
  }
}
