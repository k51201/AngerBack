package ru.vampa.angerback.db

import cats.effect.Async
import com.mongodb.client.model.Aggregates._
import com.mongodb.client.model.Filters.{eq => equal, _}
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.Projections._
import com.mongodb.client.model.Updates._
import com.mongodb.client.{MongoCollection, MongoDatabase}
import org.mongodb.scala.bson.collection.Document
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.bson.{BsonArray, ObjectId}
import ru.vampa.angerback.db.models.{DialogEntity, IdEntity, UserEntity}

import scala.jdk.CollectionConverters._

class DialogRepository[F[_] : Async](db: MongoDatabase) {
  type DialogId = String
  type UserId = String

  private val dialogs: MongoCollection[DialogEntity] =
    db.getCollection("dialogs", DialogEntity.getClass)

  def upsert(fromUser: UserId, toUser: UserId): F[DialogId] = {
    val filterBson = or(
      and(
        equal("fromUser", new ObjectId(fromUser)),
        equal("toUser", new ObjectId(toUser))
      ),
      and(
        equal("fromUser", new ObjectId(toUser)),
        equal("toUser", new ObjectId(fromUser))
      )
    )
    val updateBson = combine(
      setOnInsert("fromUser", new ObjectId(fromUser)),
      setOnInsert("toUser", new ObjectId(toUser))
    )
    val options = new FindOneAndUpdateOptions()
      .upsert(true)
      .projection(fields(include("_id")))

    val dialogsToId = dialogs.withDocumentClass[IdEntity](IdEntity.getClass[IdEntity])

    Async[F].pure {
      Option(dialogsToId.findOneAndUpdate(filterBson, updateBson, options))
        .map(_.id.toString)
    }
  }

  def findForUser(id: String): F[Seq[DialogEntity]] = {
    val oid = new ObjectId(id)
    val filterBson = or(equal("fromUser", oid), equal("fromUser", oid))

    Async[F].pure {
      dialogs.aggregate(aggregationPipeline(filterBson))
    }
  }

  def findById(id: String): F[Option[DialogEntity]] = {
    val oid = new ObjectId(id)
    val aggregationBson = aggregationPipeline(equal("_id", oid))
    Async[F].pure {
      dialogs.aggregate(aggregationBson).asScala.headOption
    }
  }

  private def aggregationPipeline(filterBson: Bson): java.util.List[Bson] = {
    Seq(
      pullByFilter(filterBson),
      lookup("users", "fromUser", "_id", "fromUser_t"),
      lookup("users", "toUser", "_id", "toUser_t"),
      project(
        fields(
          computed(
            "fromUser",
            Document("$arrayElemAt" -> BsonArray("$fromUser_t", 0))
              .toBsonDocument(classOf[UserEntity], db.getCodecRegistry)),
          computed(
            "toUser",
            Document("$arrayElemAt" -> BsonArray("$toUser_t", 0))
              .toBsonDocument(classOf[UserEntity], db.getCodecRegistry))
        ))
    ).asJava
  }
}
