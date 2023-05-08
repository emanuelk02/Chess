import dependencies._

lazy val root = project
    .in(file("."))
    .settings(
        name := sys.env.get("PROJECT").getOrElse("Chess"),
        scalaVersion := sys.env.get("SCALA_VERSION").getOrElse("3.2.2"),
        libraryDependencies ++= commonDependency,
        ThisBuild / assemblyMergeStrategy := {   
          case PathList("reference.conf") => MergeStrategy.concat
          case PathList("META-INF", xs @ _*) => MergeStrategy.discard
          case PathList("module-info.class") => MergeStrategy.discard
          case x => 
            val oldStrategy = (assembly / assemblyMergeStrategy).value
            oldStrategy(x)
        },
    )
