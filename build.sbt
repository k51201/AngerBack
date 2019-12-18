name := "WebApp1"
version := "0.1"
scalaVersion := "2.12.10"

val http4sVersion = "0.20.15"
val circeVersion = "0.12.3"
val logbackVersion = "1.2.3"
val catsVersion = "2.0.0"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,

  "org.typelevel" %% "cats-effect" % catsVersion,

  "ch.qos.logback" % "logback-classic" % logbackVersion,

  "org.scalatest" %% "scalatest" % "3.1.0" % Test
)