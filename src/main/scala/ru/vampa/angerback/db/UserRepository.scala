package ru.vampa.angerback.db

import cats.effect.{Async, ContextShift, IO}
import org.bson.types.ObjectId
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.{MongoCollection, MongoDatabase}
import ru.vampa.angerback.db.models.UserEntity

import scala.concurrent.ExecutionContext

class UserRepository[F[_] : Async](db: MongoDatabase) {
  type UserId = String

  implicit private val csIO: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  private val users: MongoCollection[UserEntity] = db.getCollection("users")

  def findUser(email: String): F[Option[UserEntity]] = Async[F].liftIO {
    val query = users.find(equal("email", email)).first()
    IO.fromFuture(IO(query.headOption()))
  }

  def createUser(user: UserEntity): F[UserId] = Async[F].liftIO {
    val query = users.insertOne(user)
    IO.fromFuture(IO(query.toFuture)).map(_.getInsertedId.asObjectId.getValue.toString)
  }

  def findUserById(id: UserId): F[Option[UserEntity]] = Async[F].liftIO {
    val query = users.find(equal("_id", new ObjectId(id))).first()
    IO.fromFuture(IO(query.headOption()))
  }

  def findUsers(): F[Seq[UserEntity]] = Async[F].liftIO {
    IO.fromFuture(IO(users.find().toFuture))
  }

  def findUsers(q: String): F[Seq[UserEntity]] = Async[F].liftIO {
    val query = users.find(regex("username", s"(?i)$q".r))
    IO.fromFuture(IO(query.toFuture))
  }
}
