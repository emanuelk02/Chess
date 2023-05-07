import dependencies._

lazy val root = project
    .in(file("."))
    .settings(
        name := "legality",
        scalaVersion := sys.env.get("SCALA_VERSION").getOrElse("3.2.2"),
        libraryDependencies ++= commonDependency,
        ThisBuild / assemblyMergeStrategy := {   
          case PathList("reference.conf") => MergeStrategy.concat
          case PathList("META-INF", xs @ _*) => MergeStrategy.discard   
          case x => MergeStrategy.first
        }
    )
