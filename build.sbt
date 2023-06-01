import dependencies._

lazy val commonSettings = Seq(
  ThisBuild / Compile / javaOptions += "-Xmx2G",
  scalaVersion := scala3Version,
  libraryDependencies ++= commonDependency,
  ThisBuild / assemblyMergeStrategy := {
    case PathList("reference.conf")    => MergeStrategy.concat
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case PathList("module-info.class") => MergeStrategy.discard
    case x =>
      val oldStrategy =
        (_root_.sbtassembly.AssemblyPlugin.autoImport.assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  },
  jacocoReportSettings := JacocoReportSettings(
    "Jacoco Coverage Report",
    None,
    JacocoThresholds(),
    Seq(
      JacocoReportFormats.ScalaHTML,
      JacocoReportFormats.XML
    ), // note XML formatter
    "utf-8"
  ),
  jacocoExcludes := Seq(
    "*aview.*",
    "*Chess.*",
    "*GameData.*",
    "*ControllerInterface.*",
    "*Service*"
  ),
  jacocoCoverallsServiceName := "github-actions",
  jacocoCoverallsBranch := sys.env.get("CI_BRANCH"),
  jacocoCoverallsPullRequest := sys.env.get("GITHUB_EVENT_NAME"),
  jacocoCoverallsRepoToken := sys.env.get("COVERALLS_REPO_TOKEN")
)

lazy val utils = project
  .in(file("utils"))
  .settings(
    name := "utils",
    commonSettings
  )
  .enablePlugins(JacocoCoverallsPlugin)

lazy val legality = project
  .in(file("legality"))
  .settings(
    name := "legality",
    commonSettings,
    Defaults.itSettings,
    IntegrationTest / fork := true,
    libraryDependencies ++= Seq(
      scalatest_it,
      gatlingHigh,
      gatlingTest
    )
  )
  .enablePlugins(JacocoCoverallsPlugin, GatlingPlugin)
  .dependsOn(utils)

lazy val persistence: Project = project
  .in(file("persistence"))
  .settings(
    name := "persistence",
    commonSettings,
    Test / parallelExecution := false,
    Defaults.itSettings,
    IntegrationTest / fork := true,
    libraryDependencies ++= Seq(
      scalatest_it,
      gatlingHigh,
      gatlingTest
    )
  )
  .enablePlugins(JacocoCoverallsPlugin, GatlingPlugin)
  .dependsOn(utils)

lazy val controller = project
  .in(file("controller"))
  .settings(
    name := "controller",
    commonSettings,
    Defaults.itSettings,
    IntegrationTest / fork := true,
    libraryDependencies ++= Seq(
      scalatest_it,
      gatlingHigh,
      gatlingTest
    )
  )
  .enablePlugins(JacocoCoverallsPlugin, GatlingPlugin)
  .dependsOn(utils, persistence, legality)

lazy val ui = project
  .in(file("ui"))
  .settings(
    name := "ui",
    commonSettings
  )
  .enablePlugins(JacocoCoverallsPlugin)
  .dependsOn(utils, persistence, legality, controller)

lazy val root = project
  .in(file("."))
  .settings(
    name := "Chess",
    version := "2.0.0",
    commonSettings,
    Defaults.itSettings,
    IntegrationTest / fork := true,
    libraryDependencies ++= Seq(
      scalatest_it,
      gatlingHigh,
      gatlingTest
    )
  )
  .enablePlugins(JacocoCoverallsPlugin, GatlingPlugin)
  .aggregate(utils, persistence, legality, controller, ui)
  .dependsOn(utils, persistence, legality, controller, ui)
