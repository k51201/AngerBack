package ru.vampa.angerback.dto

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class RegRequest(
  username: String,
  email: String,
  password: String,
  repeatpassword: String
)

object RegRequest {
  implicit val jsonDec: Decoder[RegRequest] = deriveDecoder[RegRequest]
}
