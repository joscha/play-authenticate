organization := "com.feth"

name := "play-authenticate-simple-oauth"

scalaVersion := "2.11.2"

version := "1.0-SNAPSHOT"

val appDependencies = Seq(
  "com.feth" %% "play-authenticate" % "0.6.7",
  javaCore,
  cache,
  javaWs,
  javaEbean
)

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    libraryDependencies ++= appDependencies
  )
