name := """play-1"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.0"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0"
libraryDependencies += "com.github.tminglei" %% "slick-pg" % "0.16.2"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.24"
libraryDependencies += "org.json4s" %% "json4s-native" % "3.5.3"

libraryDependencies += "com.sun.jersey" % "jersey-core" % "1.19.4"
libraryDependencies += "com.sun.jersey" % "jersey-client" % "1.19.4"
libraryDependencies += "com.sun.jersey.contribs" % "jersey-multipart" % "1.19.4"


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
