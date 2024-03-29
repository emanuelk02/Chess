import dependencies._

ThisBuild / Compile / scalaVersion := scala3Version

lazy val commonSettings = Seq(
    scalaVersion := scala3Version,
    libraryDependencies ++= commonDependency,
    ThisBuild / Compile / javaOptions += "-Xmx4G",
    ThisBuild / assemblyMergeStrategy := {   
      case PathList("reference.conf") => MergeStrategy.concat
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case PathList("module-info.class") => MergeStrategy.discard
      case x => 
        val oldStrategy = (_root_.sbtassembly.AssemblyPlugin.autoImport.assembly / assemblyMergeStrategy).value
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

lazy val persistence = project
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

lazy val chess = project
    .in(file("chess"))
    .settings(
        name := "chess",
        commonSettings
    )
    .dependsOn(utils, persistence, legality, controller, ui)


lazy val root = project
    .in(file("."))
    .settings(
        name := sys.env.get("SERVICE").get,
        commonSettings
    )
    .dependsOn(sys.env.get("SERVICE").get match {
        case "utils" => utils
        case "legality" => legality
        case "persistence" => persistence
        case "controller" => controller
        case "ui" => ui
        case "chess" => chess
    })