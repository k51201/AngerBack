package ru.vampa.angerback.dto

import ru.vampa.angerback.db.models.UserEntity

case class User(
    id: String,
    username: String,
    email: String
)

object User {
  def apply(e: UserEntity): User = User(e._id.toString, e.username, e.email)
}
