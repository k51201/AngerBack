package ru.vampa.angerback.db

import cats.effect.{Async, ContextShift, IO}
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.FindOneAndUpdateOptions
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.{MongoCollection, MongoDatabase}
import ru.vampa.angerback.db.models.DialogEntity

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
    val options = new FindOneAndUpdateOptions().upsert(true)

    Async[F].liftIO {
      val query = dialogs.findOneAndUpdate(filterBson, updateBson, options)
      IO.fromFuture(IO(query.toFuture)).map(_.id.toString)
    }
  }
}
