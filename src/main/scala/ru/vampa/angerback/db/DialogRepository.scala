package ru.vampa.angerback.db

import cats.effect.Async
import org.mongodb.scala.bson.collection.Document
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.bson.{BsonArray, ObjectId}
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.FindOneAndUpdateOptions
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.{MongoCollection, MongoDatabase}
import ru.vampa.angerback.db.models.{DialogEntity, IdEntity, UserEntity}

class DialogRepository[F[_] : Async](db: MongoDatabase) {
  type DialogId = String
  type UserId = String

  private val dialogs: MongoCollection[DialogEntity] =
    db.getCollection[DialogEntity]("dialogs")

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

    val dialogsToId = dialogs.withDocumentClass[IdEntity]()

    Async[F].fromFuture {
      Async[F].pure {
        dialogsToId.findOneAndUpdate(filterBson, updateBson, options)
          .map(_.id.toString)
          .head()
      }
    }
  }

  def findForUser(id: String): F[Seq[DialogEntity]] = {
    val oid = new ObjectId(id)
    val filterBson = or(equal("fromUser", oid), equal("fromUser", oid))

    Async[F].fromFuture {
      Async[F].pure {
        dialogs.aggregate(aggregationPipeline(filterBson)).toFuture()
      }
    }
  }

  def findById(id: String): F[Option[DialogEntity]] = {
    val oid = new ObjectId(id)
    val aggregationBson = aggregationPipeline(equal("_id", oid))

    Async[F].fromFuture {
      Async[F].pure {
        dialogs.aggregate(aggregationBson).headOption()
      }
    }
  }

  private def aggregationPipeline(filterBson: Bson): Seq[Bson] = {
    Seq(
      pullByFilter(filterBson),
      lookup("users", "fromUser", "_id", "fromUser_t"),
      lookup("users", "toUser", "_id", "toUser_t"),
      project(
        fields(
          computed(
            "fromUser",
            Document("$arrayElemAt" -> BsonArray("$fromUser_t", 0))
              .toBsonDocument(classOf[UserEntity], db.codecRegistry)),
          computed(
            "toUser",
            Document("$arrayElemAt" -> BsonArray("$toUser_t", 0))
              .toBsonDocument(classOf[UserEntity], db.codecRegistry))
        ))
    )
  }
}
