package ru.vampa.angerback.db

import cats.effect.Async
import org.mongodb.scala.model.Filters._
import org.bson.types.ObjectId
import org.mongodb.scala.{MongoCollection, MongoDatabase}
import ru.vampa.angerback.db.models.UserEntity

import scala.concurrent.ExecutionContext

class UserRepository[F[_] : Async](db: MongoDatabase)(implicit ec: ExecutionContext) {
  private val users: MongoCollection[UserEntity] = db.getCollection("users")

  def findOne(email: String): F[Option[UserEntity]] = Async[F].fromFuture {
    Async[F].pure {
      users.find(equal("email", email)).headOption()
    }
  }

  def create(user: UserEntity): F[String] = Async[F].fromFuture {
    Async[F].pure {
      users.insertOne(user).head().map(_.getInsertedId.asObjectId().getValue.toString)
    }
  }

  def findById(id: String): F[Option[UserEntity]] = Async[F].fromFuture {
    Async[F].pure {
      users.find(equal("_id", new ObjectId(id))).headOption()
    }
  }

  def findAll(): F[Seq[UserEntity]] = Async[F].fromFuture {
    Async[F].pure {
      users.find().toFuture()
    }
  }

  def find(q: String): F[Seq[UserEntity]] = Async[F].fromFuture {
    Async[F].pure {
      users.find(regex("username", s"(?i)$q")).toFuture()
    }
  }
}
