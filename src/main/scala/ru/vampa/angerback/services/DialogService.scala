package ru.vampa.angerback.services

import cats.effect.Sync
import cats.implicits._
import ru.vampa.angerback.db.DialogRepository

class DialogService[F[_]: Sync](dialogRepo: DialogRepository[F]) {
  type DialogId = String
  type UserId = String

  def createDialog(fromUser: UserId, toUser: UserId): F[Either[String, DialogId]] = {
    dialogRepo.upsertDialog(fromUser, toUser)
      .map(_.asRight[String])
      .recover {
        case e => Either.left[String, DialogId](e.getMessage)
      }
  }
}
