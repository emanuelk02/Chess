import dependencies._

lazy val commonSettings = Seq(
    scalaVersion := scala3Version,
    libraryDependencies ++= commonDependency,
    ThisBuild / assemblyMergeStrategy := {   
      case PathList("reference.conf") => MergeStrategy.concat
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard   
      case x => MergeStrategy.first
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
    ).dependsOn(utils)

lazy val persistence = project
    .in(file("persistence"))
    .settings(
        name := "persistence",
        commonSettings
    ).dependsOn(utils)

lazy val root = project
    .in(file("."))
    .settings(
        name := sys.env.get("PROJECT").getOrElse("Chess"),
        commonSettings
    ).dependsOn(utils)
    .dependsOn(legality)
    .dependsOn(persistence)