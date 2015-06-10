organization := "com.feth"

name := "play-authenticate-tests"

scalaVersion := "2.11.6"

version := "1.0-SNAPSHOT"

val appDependencies = Seq(
  "com.feth"      %% "play-authenticate" % "0.7.0-SNAPSHOT",
  specs2 % "test",
  "org.easytesting" % "fest-assert" % "1.4" % "test"
)

// add resolver for easymail snapshots
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    libraryDependencies ++= appDependencies
  )
