package ru.vampa.angerback.dto

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class FormResponse(
  Success: Boolean,
  error: Option[String]
)

object FormResponse {
  implicit val jsonEnc: Encoder[FormResponse] = deriveEncoder[FormResponse]

  def apply(): FormResponse = new FormResponse(true, None)
  def apply(error: String): FormResponse = new FormResponse(false, Some(error))
}
