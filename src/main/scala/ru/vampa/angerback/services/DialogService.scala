package ru.vampa.angerback.services

import cats.effect.Sync
import cats.implicits._
import ru.vampa.angerback.db.DialogRepository
import ru.vampa.angerback.dto.Dialog

class DialogService[F[_]: Sync](dialogRepo: DialogRepository[F]) {
  type DialogId = String
  type UserId = String

  def createDialog(fromUser: UserId, toUser: UserId): F[Either[String, DialogId]] = {
    dialogRepo.upsert(fromUser, toUser)
      .map(_.asRight[String])
      .recover {
        case e => Either.left[String, DialogId](e.getMessage)
      }
  }

  def userDialogs(id: String): F[Either[String, Seq[Dialog]]] = {
    dialogRepo.findForUser(id)
      .map(_.map(Dialog.from(id)).asRight[String])
      .recover {
        case e => Either.left[String, Seq[Dialog]](e.getMessage)
      }
  }

  def getDialogForUser(dialogId: String, userId: String): F[Either[String, Dialog]] = {
    dialogRepo.findById(dialogId)
      .map(_.toRight("Dialog not found").map(Dialog.from(userId)))
      .recover {
        case e => Either.left[String, Dialog](e.getMessage)
      }
  }
}
