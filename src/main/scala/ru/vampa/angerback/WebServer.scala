package ru.vampa.angerback

import cats.effect._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.http4s.HttpApp
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import org.mongodb.scala.bson.codecs.Macros
import org.mongodb.scala.{MongoClient, MongoDatabase}
import ru.vampa.angerback.db.{DialogRepository, UserRepository}
import ru.vampa.angerback.db.models.{DialogEntity, IdEntity, UserEntity}
import ru.vampa.angerback.services.{DialogService, UserService}

import scala.concurrent.ExecutionContext.global

object WebServer extends IOApp {
  val db: MongoDatabase = database
  val userRepo = new UserRepository[IO](db)
  val dialogRepo = new DialogRepository[IO](db)

  val userService: UserService[IO] = new UserService[IO](userRepo)
  val dialogService: DialogService[IO] = new DialogService[IO](dialogRepo)

  val httpApp: HttpApp[IO] = Router(
    "/api" -> new ApiRouter[IO](userService, dialogService).routes
  ).orNotFound

  override def run(args: List[String]): IO[ExitCode] = {
    BlazeServerBuilder[IO](global)
      .bindHttp(3001, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve.compile.drain
      .as(ExitCode.Success)
  }

  def database: MongoDatabase = {
    val codecRegistry: CodecRegistry = fromRegistries(
      fromProviders(Macros.createCodecProvider[UserEntity]()),
      fromProviders(Macros.createCodecProvider[DialogEntity]()),
      fromProviders(Macros.createCodecProvider[IdEntity]()),
      MongoClient.DEFAULT_CODEC_REGISTRY
    )
    val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017")
    mongoClient.getDatabase("angermess").withCodecRegistry(codecRegistry)
  }
}
