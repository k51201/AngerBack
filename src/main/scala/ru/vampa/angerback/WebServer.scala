package ru.vampa.angerback

import cats.effect._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.http4s.HttpApp
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import org.mongodb.scala.{MongoClient, MongoDatabase}
import ru.vampa.angerback.db.UserRepository
import ru.vampa.angerback.db.models.UserEntity

import scala.concurrent.ExecutionContext.global

object WebServer extends IOApp {
  val codecRegistry: CodecRegistry = fromRegistries(
    fromProviders(UserEntity.codecProvider),
    MongoClient.DEFAULT_CODEC_REGISTRY
  )
  val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017")
  val db: MongoDatabase = mongoClient.getDatabase("angermess").withCodecRegistry(codecRegistry)

  val userRepo = new UserRepository[IO](db)
  val service: Service[IO] = new Service[IO](userRepo)

  val httpApp: HttpApp[IO] = Router(
    "/api" -> new ApiRouter[IO](service).routes
  ).orNotFound

  override def run(args: List[String]): IO[ExitCode] = {
    BlazeServerBuilder[IO](global)
      .bindHttp(3001, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve.compile.drain
      .as(ExitCode.Success)
  }
}
