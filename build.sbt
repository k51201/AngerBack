name := "AngerBack"
version := "0.1"
scalaVersion := "2.13.2"

val http4sVersion = "0.21.4"
val circeVersion = "0.13.0"
val logbackVersion = "1.2.3"
val catsVersion = "2.1.3"

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

  "org.mongodb.scala" %% "mongo-scala-driver" % "4.0.4",
  "com.github.t3hnar" %% "scala-bcrypt" % "4.1",
  "org.reactormonk" %% "cryptobits" % "1.3",

  "org.scalatest" %% "scalatest" % "3.1.0" % Test
)

addCompilerPlugin("org.typelevel" % "kind-projector_2.13.2" % "0.11.0")
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
