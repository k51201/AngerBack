package ru.vampa.angerback.db.models

import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.annotations.BsonProperty

case class IdEntity(@BsonProperty("_id") id: ObjectId)
