package ru.vampa.angerback.db

import cats.effect.{Async, ContextShift, IO}
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.{MongoCollection, MongoDatabase}
import ru.vampa.angerback.db.models.UserEntity

import scala.concurrent.ExecutionContext

class UserRepository[F[_] : Async](db: MongoDatabase) {
  implicit private val csIO: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  private val users: MongoCollection[UserEntity] = db.getCollection("users")

  def findUser(email: String): F[Option[UserEntity]] = Async[F].liftIO {
    val query = users.find(equal("email", email)).first()
    IO.fromFuture(IO(query.headOption()))
  }

  def createUser(user: UserEntity): F[String] = Async[F].liftIO {
    val query = users.insertOne(user)
    IO.fromFuture(IO(query.toFuture)).map(_.getInsertedId.asObjectId.getValue.toString)
  }
}
