import sbt._
import Keys._


object dependencies {
    val scala3Version = "3.2.2"
    val AkkaVersion = "2.7.0"
    val AkkaHttpVersion = "10.5.1"

    val scalactic = "org.scalactic" %% "scalactic" % "3.2.10"
    val scalatest = "org.scalatest" %% "scalatest" % "3.2.10" % "test"
    val akkaActor = "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
    val akkaSteam = "com.typesafe.akka" %% "akka-stream" % AkkaVersion
    val akkaHttp  = "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
    val akkaStreamTest = "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion
    val akkaHttpTest   = "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion
    val akkaSprayJson  = "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion
    val slf4j     = "org.slf4j" % "slf4j-simple" % "2.0.7"
    val sprayJson = "io.spray" %%  "spray-json" % "1.3.6"
    val playJson  = ("com.typesafe.play" %% "play-json" % "2.9.3").cross(CrossVersion.for3Use2_13)

    val commonDependency = Seq(
        scalactic,
        scalatest,
        akkaActor,
        akkaSteam,
        akkaHttp,
        akkaStreamTest,
        akkaHttpTest,
        akkaSprayJson,
        slf4j,
        sprayJson,
        playJson
    )
}