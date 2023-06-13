package ru.vampa.angerback.db.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

case class IdEntity(@BsonId id: ObjectId)
