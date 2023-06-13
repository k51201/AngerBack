package ru.vampa.angerback.db

import cats.effect.Async
import com.mongodb.client.model.Filters.{eq => equal, _}
import com.mongodb.client.{MongoCollection, MongoDatabase}
import org.bson.types.ObjectId
import ru.vampa.angerback.db.models.UserEntity

class UserRepository[F[_] : Async](db: MongoDatabase) {
  type UserId = String

  private val users: MongoCollection[UserEntity] = db.getCollection("users", UserEntity.getClass)

  def findOne(email: String): F[Option[UserEntity]] = Async[F].pure {
    Option(users.find(equal("email", email)).first())
  }

  def create(user: UserEntity): F[UserId] = Async[F].pure {
    Option(users.insertOne(user).getInsertedId)
      .map(insertedId => insertedId.asObjectId().getValue.toString)
  }

  def findById(id: UserId): F[Option[UserEntity]] = Async[F].pure {
    Option(users.find(equal("_id", new ObjectId(id))).first())
  }

  def findAll(): F[Seq[UserEntity]] = Async[F].pure {
    users.find()
  }

  def find(q: String): F[Seq[UserEntity]] = Async[F].pure {
    users.find(regex("username", s"(?i)$q"))
  }
}
