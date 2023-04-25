val scala3Version = "3.2.2"

val AkkaVersion = "2.7.0"
val AkkaHttpVersion = "10.5.1"
lazy val commonDependency = Seq(
    "com.novocode" % "junit-interface" % "0.11" % "test",
    "org.scalactic" %% "scalactic" % "3.2.10",
    "org.scalatest" %% "scalatest" % "3.2.10" % "test",
    "com.google.inject" % "guice" % "4.2.3",
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
    "org.slf4j" % "slf4j-simple" % "2.0.7",
    "io.spray" %%  "spray-json" % "1.3.6",
    ("com.typesafe.play" %% "play-json" % "2.9.3").cross(CrossVersion.for3Use2_13)
)

lazy val commonSettings = Seq(
    scalaVersion := scala3Version,
    libraryDependencies ++= commonDependency,
    libraryDependencies += ("net.codingwell" %% "scala-guice" % "5.0.2").cross(CrossVersion.for3Use2_13),
    
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
        commonSettings,
        libraryDependencies += ("org.scala-lang.modules" %% "scala-xml" % "2.0.1")
    )
    .enablePlugins(JacocoCoverallsPlugin)
    .dependsOn(utils)


lazy val controller = project
    .in(file("controller"))
    .settings(
        name := "controller",
        commonSettings,
        libraryDependencies += ("org.scala-lang.modules" %% "scala-swing" % "3.0.0").cross(CrossVersion.for3Use2_13)
    )
    .enablePlugins(JacocoCoverallsPlugin)
    .dependsOn(utils, persistence, legality)

lazy val ui = project
    .in(file("ui"))
    .settings(
        name := "ui",
        commonSettings,
        libraryDependencies += ("org.scala-lang.modules" %% "scala-swing" % "3.0.0").cross(CrossVersion.for3Use2_13)
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
