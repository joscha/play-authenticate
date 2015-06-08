organization := "com.feth"

name := "play-authenticate-simple-oauth"

scalaVersion := "2.11.6"

version := "1.0-SNAPSHOT"

val appDependencies = Seq(
  "com.feth" %% "play-authenticate" % "0.7.0",
  javaCore,
  cache,
  javaWs
)

lazy val `play-authenticate-simple-oauth` = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)
  .settings(
    libraryDependencies ++= appDependencies
  )
