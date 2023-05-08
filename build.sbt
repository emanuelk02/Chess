import dependencies._

lazy val commonSettings = Seq(
    scalaVersion := scala3Version,
    libraryDependencies ++= commonDependency
    
    jacocoReportSettings := JacocoReportSettings(
      "Jacoco Coverage Report",
      None,
      JacocoThresholds(),
      Seq(JacocoReportFormats.ScalaHTML, JacocoReportFormats.XML), // note XML formatter
    "utf-8"),
    
    jacocoExcludes := Seq(
      "*aview.*",
      "*Chess.*",
      "*GameData.*",
      "*ControllerInterface.*"
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
        commonSettings
    )
    .enablePlugins(JacocoCoverallsPlugin)
    .dependsOn(utils)


lazy val persistence: Project = project
    .in(file("persistence"))
    .settings(
        name := "persistence",
        commonSettings
    )
    .enablePlugins(JacocoCoverallsPlugin)
    .dependsOn(utils)


lazy val controller = project
    .in(file("controller"))
    .settings(
        name := "controller",
        commonSettings
    )
    .enablePlugins(JacocoCoverallsPlugin)
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

    commonSettings
  )
  .enablePlugins(JacocoCoverallsPlugin)
  .aggregate(utils, persistence, legality, controller, ui)
  .dependsOn(utils, persistence, legality, controller, ui)
