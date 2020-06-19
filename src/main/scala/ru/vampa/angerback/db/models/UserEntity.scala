package ru.vampa.angerback.db.models

import org.bson.codecs.configuration.CodecProvider
import org.bson.types.ObjectId
import org.mongodb.scala.bson.codecs.Macros

case class UserEntity(
  username: String,
  email: String,
  password: String,
  _id: ObjectId = new ObjectId()
)

object UserEntity {
  val codecProvider: CodecProvider = Macros.createCodecProvider[UserEntity]()
}
