package ru.vampa.angerback.dto

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class User(
  id: String,
  username: String,
  email: String
)

object User {
  implicit val jsonEnc: Encoder[User] = deriveEncoder[User]
}
