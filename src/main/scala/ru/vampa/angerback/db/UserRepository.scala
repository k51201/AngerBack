package ru.vampa.angerback.db

import cats.effect.{Async, ContextShift, IO}
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import ru.vampa.angerback.db.models.UserEntity

import scala.concurrent.ExecutionContext

class UserRepository[F[_]: Async] {
  implicit private val csIO: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val codecRegistry: CodecRegistry = fromRegistries(fromProviders(UserEntity.codecProvider), MongoClient.DEFAULT_CODEC_REGISTRY)

  private val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017")
  private val db: MongoDatabase = mongoClient.getDatabase("angermess").withCodecRegistry(codecRegistry)
  private val users: MongoCollection[UserEntity] = db.getCollection("users")

  def findUser(email: String): F[Option[UserEntity]] = Async[F].liftIO {
    val query = users.find(equal("email", email)).first()
    IO.fromFuture(IO(query.headOption()))
  }

  def createUser(user: UserEntity): F[Unit] = Async[F].liftIO {
    val query = users.insertOne(user)
    IO.fromFuture(IO(query.toFuture)).map(_ => ())
  }
}
