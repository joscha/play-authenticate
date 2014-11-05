import play.PlayJava

organization := "com.feth"

name := "play-authenticate-tests"

scalaVersion := "2.11.2"

version := "1.0-SNAPSHOT"

lazy val playAuthenticate = (project in file("modules/play-authenticate")).enablePlugins(PlayJava)

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .dependsOn(playAuthenticate)
  .aggregate(playAuthenticate)
