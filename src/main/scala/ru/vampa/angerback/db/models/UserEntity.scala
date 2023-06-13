package ru.vampa.angerback.db.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

case class UserEntity(
  username: String,
  email: String,
  password: String,
  @BsonId id: ObjectId = new ObjectId()
)
