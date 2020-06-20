package ru.vampa.angerback.dto

import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.{Encoder, JsonObject}

sealed trait FormResponse

object FormResponse {
  case object Success extends FormResponse
  case class Error(error: String) extends FormResponse

  implicit val jsonEnc: Encoder[FormResponse] = Encoder.instance {
    case obj: Error => obj.asJsonObject.add("Success", false.asJson).asJson
    case Success    => JsonObject.empty.add("Success", true.asJson).asJson
  }

  def success: FormResponse = Success
  def error(msg: String): FormResponse = Error(msg)
}
