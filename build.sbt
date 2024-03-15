ThisBuild / version := "0.1.0-SNAPSHOT"

val PekkoVersion = "1.0.2"
val PekkoHttpVersion = "1.0.1"

lazy val backend = (project in file("backend"))
  .settings(
    scalaVersion := "2.13.13",
    name := "Reviews",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-actor-typed" % PekkoVersion,
      "org.apache.pekko" %% "pekko-stream" % PekkoVersion,
      "org.apache.pekko" %% "pekko-http" % PekkoHttpVersion,
      "io.spray" %%  "spray-json" % "1.3.6",
      "com.typesafe.slick" %% "slick" % "3.4.1",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.4.1",
      "org.slf4j" % "slf4j-nop" % "2.0.9",
      "com.h2database" % "h2" % "2.2.224",
      "org.postgresql" % "postgresql" % "42.7.1",
      "com.github.tminglei" %% "slick-pg" % "0.21.1",
      "com.github.tminglei" %% "slick-pg_play-json" % "0.21.1"

    )
  )

lazy val frontend = (project in file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion := "3.3.3",
    name := "Frontend",
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
    },
    // Compile / mainClass := Some("App"),
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "com.raquo" %%% "laminar" % "16.0.0"
    )
  )
