package ru.vampa.angerback

import cats.effect.{Async, Sync}
import cats.implicits._
import com.github.t3hnar.bcrypt._
import ru.vampa.angerback.db.UserRepository
import ru.vampa.angerback.db.models.UserEntity
import ru.vampa.angerback.dto.{FormResponse, RegRequest}

class Service[F[_]: Async](repo: UserRepository[F]) {
  def registration(reg: RegRequest): F[FormResponse] = {
    if (reg.username.isEmpty || reg.email.isEmpty || reg.password.isEmpty || reg.repeatpassword.isEmpty) {
      Sync[F].pure(FormResponse("Все поля должны быть заполнены!"))
    } else if (reg.password != reg.repeatpassword) {
      Sync[F].pure(FormResponse("Пароли не совпадают!"))
    } else {
      repo.findUser(reg.email).flatMap {
        case Some(_) => Sync[F].pure(FormResponse("Имя занято!"))
        case None =>
          val hash = reg.password.bcrypt(8)
          repo.createUser(UserEntity(reg.username, reg.email, hash))
            .map(_ => FormResponse())
      }
    }
  }

  }
