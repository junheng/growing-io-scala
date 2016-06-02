lazy val `growing-io-scala` = Project(id = "growing-io-scala", base = file("."))
  .settings(
    name := "growing-io-scala",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.10.6",
    ivyScala := ivyScala.value map {
      _.copy(overrideScalaVersion = true)
    }, libraryDependencies ++= Seq(
      //for AKKA
      "com.typesafe.akka" %% "akka-actor" % "2.3.15" withSources(),
      "com.typesafe.akka" %% "akka-kernel" % "2.3.15" withSources(),
      "com.typesafe.akka" %% "akka-slf4j" % "2.3.15" withSources(),
      "com.typesafe.akka" %% "akka-contrib" % "2.3.15" withSources(),
      //for Http Client
      "net.databinder.dispatch" % "dispatch-core_2.11" % "0.11.3" withSources(),
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
