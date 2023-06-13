name := "AngerBack"
version := "0.1"
scalaVersion := "2.13.11"

val http4sVersion = "0.23.11"
val circeVersion = "0.14.1"
val logbackVersion = "1.2.11"
val catsVersion = "3.3.8"
val mongoVersion = "4.9.1"

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

  "org.mongodb" % "mongodb-driver-sync" % mongoVersion,
  "org.mongodb.scala" %% "mongo-scala-bson" % mongoVersion,
  "com.github.t3hnar" %% "scala-bcrypt" % "4.3.0",
  "org.reactormonk" %% "cryptobits" % "1.3.1",

  "org.scalatest" %% "scalatest" % "3.2.11" % Test
)

addCompilerPlugin("org.typelevel" % "kind-projector_2.13.2" % "0.13.2")
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
