package ru.vampa.angerback.db.models

import org.bson.types.ObjectId
import org.mongodb.scala.bson.annotations.BsonProperty

case class DialogEntity(
    fromUser: ObjectId,
    toUser: ObjectId,
    @BsonProperty("_id") id: ObjectId = new ObjectId()
)
