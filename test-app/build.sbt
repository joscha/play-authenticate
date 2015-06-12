organization := "com.feth"

name := "play-authenticate-tests"

scalaVersion := "2.11.6"

version := "1.0-SNAPSHOT"

val appDependencies = Seq(
  specs2 % "test",
  "org.easytesting" % "fest-assert" % "1.4" % "test"
)

// Needed for specs2
resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

// add resolver for easymail snapshots
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val playAuthenticate = (project in file("modules/play-authenticate")).enablePlugins(PlayJava)

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    libraryDependencies ++= appDependencies
  )
  .dependsOn(playAuthenticate)
  .aggregate(playAuthenticate)
