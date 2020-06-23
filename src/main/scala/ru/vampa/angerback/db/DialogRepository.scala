package ru.vampa.angerback.db

import cats.effect.{Async, ContextShift, IO}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.bson.{BsonArray, ObjectId}
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.FindOneAndUpdateOptions
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.{Document, MongoCollection, MongoDatabase}
import ru.vampa.angerback.db.models.{DialogEntity, IdEntity, UserEntity}

import scala.concurrent.ExecutionContext

class DialogRepository[F[_]: Async](db: MongoDatabase) {
  type DialogId = String
  type UserId = String

  implicit private val csIO: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)
  private val dialogs: MongoCollection[DialogEntity] =
    db.getCollection("dialogs")

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

    Async[F].liftIO {
      val query = dialogs
        .withDocumentClass[IdEntity]()
        .findOneAndUpdate(filterBson, updateBson, options)
      IO.fromFuture(IO(query.toFuture)).map(_.id.toString)
    }
  }

  def findForUser(id: String): F[Seq[DialogEntity]] = {
    val oid = new ObjectId(id)
    val filterBson = or(equal("fromUser", oid), equal("fromUser", oid))
    val aggregationBson = aggregationPipeline(filterBson)
    Async[F].liftIO {
      val query = dialogs.aggregate(aggregationBson)
      IO.fromFuture(IO(query.toFuture))
    }
  }

  def findById(id: String): F[Option[DialogEntity]] = {
    val oid = new ObjectId(id)
    val aggregationBson = aggregationPipeline(equal("_id", oid))
    Async[F].liftIO {
      val query = dialogs.aggregate(aggregationBson)
      IO.fromFuture(IO(query.headOption))
    }
  }

  private def aggregationPipeline(filterBson: Bson) = {
    Seq(
      filter(filterBson),
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
