val scala3Version = "3.0.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "Chess",
    version := "0.9.0",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq("com.novocode" % "junit-interface" % "0.11" % "test",
      "org.scalactic" %% "scalactic" % "3.2.10",
      "org.scalatest" %% "scalatest" % "3.2.10" % "test",
      "org.scala-lang.modules" %% "scala-swing" % "3.0.0"),
    
    jacocoCoverallsServiceName := "github-actions",
    jacocoCoverallsBranch := sys.env.get("CI_BRANCH"),
    jacocoCoverallsPullRequest := sys.env.get("GITHUB_EVENT_NAME"),
    jacocoCoverallsRepoToken := sys.env.get("COVERALLS_REPO_TOKEN")
  )
  .enablePlugins(JacocoCoverallsPlugin)
