package ru.vampa.angerback

import cats.effect._
import com.comcast.ip4s._
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import org.mongodb.scala.bson.codecs.Macros
import org.mongodb.scala.{MongoClient, MongoDatabase}
import ru.vampa.angerback.db.models.{DialogEntity, IdEntity, UserEntity}
import ru.vampa.angerback.db.{DialogRepository, UserRepository}
import ru.vampa.angerback.services.{DialogService, UserService}

import scala.concurrent.ExecutionContext.Implicits.global

object WebServer extends IOApp {
  val db: MongoDatabase = database
  val userRepo = new UserRepository[IO](db)
  val dialogRepo = new DialogRepository[IO](db)

  val userService: UserService[IO] = new UserService[IO](userRepo)
  val dialogService: DialogService[IO] = new DialogService[IO](dialogRepo)

  val httpApp: HttpApp[IO] = Router(
    "/api" -> new ApiRouter[IO](userService, dialogService).routes
  ).orNotFound

  val server: Resource[IO, Server] = EmberServerBuilder.default[IO]
    .withHost(ipv4"0.0.0.0")
    .withPort(port"3001")
    .withHttpApp(httpApp)
    .build

  override def run(args: List[String]): IO[ExitCode] = {
    server.use(server =>
      IO.delay(println(s"Server Has Started at ${server.address}")) >>
        IO.never.as(ExitCode.Success)
    )
  }

  def database: MongoDatabase = {
    val codecRegistry: CodecRegistry = fromProviders(
      fromProviders(Macros.createCodecProvider[UserEntity]()),
      fromProviders(Macros.createCodecProvider[DialogEntity]()),
      fromProviders(Macros.createCodecProvider[IdEntity]()),
      Bson.DEFAULT_CODEC_REGISTRY
    )
    val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017")
    mongoClient.getDatabase("angermess").withCodecRegistry(codecRegistry)
  }
}
