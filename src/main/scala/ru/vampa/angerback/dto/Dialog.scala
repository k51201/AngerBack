package ru.vampa.angerback.dto

import ru.vampa.angerback.db.models.DialogEntity

case class Dialog(
    id: String,
    user: User
)

object Dialog {
  def from(id: String)(e: DialogEntity): Dialog = {
    val userEntity = if (e.fromUser.id.toHexString == id) e.toUser else e.fromUser
    val user = User(userEntity)
    Dialog(e.id.toString, user)
  }
}
