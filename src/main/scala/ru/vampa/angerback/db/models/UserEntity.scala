package ru.vampa.angerback.db.models

import org.bson.types.ObjectId
import org.mongodb.scala.bson.annotations.BsonProperty

case class UserEntity(
    username: String,
    email: String,
    password: String,
    @BsonProperty("_id") id: ObjectId = new ObjectId()
)
