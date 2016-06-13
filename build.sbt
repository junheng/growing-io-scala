val versionOfAkka = "2.4.7"

lazy val `growing-io-scala` = Project("growing-io-scala", file("."))
  .settings(
    name := "growing-io-scala",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.7",
    ivyScala := ivyScala.value map {
      _.copy(overrideScalaVersion = true)
    }, libraryDependencies ++= Seq(
      //for AKKA
      "com.typesafe.akka" %% "akka-actor" % versionOfAkka withSources(),
      "com.typesafe.akka" %% "akka-kernel" % versionOfAkka withSources(),
      "com.typesafe.akka" %% "akka-slf4j" % versionOfAkka withSources(),
      "com.typesafe.akka" %% "akka-contrib" % versionOfAkka withSources(),
      //for Json
      "org.json4s" %% "json4s-jackson" % "3.2.11" withSources(),
      "org.json4s" %% "json4s-ext" % "3.2.11" withSources(),
      //for Http Client
      "net.databinder.dispatch" %% "dispatch-core" % "0.11.3" withSources(),
      //for MD5
      "commons-codec" % "commons-codec" % "1.4",
      //for Logger
      "org.slf4j" % "jul-to-slf4j" % "1.7.7",
      "org.slf4j" % "log4j-over-slf4j" % "1.7.7",
      "ch.qos.logback" % "logback-classic" % "1.1.3",
      //for Test
      "org.scalatest" %% "scalatest" % "2.2.5" % "test"
    )
  )
