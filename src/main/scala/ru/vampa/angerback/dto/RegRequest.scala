package ru.vampa.angerback.dto

case class RegRequest(
  username: String,
  email: String,
  password: String,
  repeatpassword: String
)
