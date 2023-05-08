import dependencies._

lazy val commonSettings = Seq(
    scalaVersion := scala3Version,
    libraryDependencies ++= commonDependency,
    ThisBuild / javaOptions += "-Xmx2G",
    ThisBuild / assemblyMergeStrategy := {   
      case PathList("reference.conf") => MergeStrategy.concat
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard   
      case x => 
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
)

lazy val utils = project
  .in(file("utils"))
  .settings(
    name := "utils",
    commonSettings
  )

lazy val legality = project
    .in(file("legality"))
    .settings(
        name := "legality",
        commonSettings
    )
    .dependsOn(utils)


lazy val persistence: Project = project
    .in(file("persistence"))
    .settings(
        name := "persistence",
        commonSettings
    )
    .dependsOn(utils)


lazy val controller = project
    .in(file("controller"))
    .settings(
        name := "controller",
        commonSettings
    )
    .dependsOn(utils, persistence, legality)

lazy val ui = project
    .in(file("ui"))
    .settings(
        name := "ui",
        commonSettings
    )
    .dependsOn(utils, persistence, legality, controller)

lazy val root = project
  .in(file("."))
  .settings(
    name := sys.env.get("PROJECT").getOrElse("Chess"),
    commonSettings
  )
  .dependsOn(utils, persistence, legality, controller, ui)