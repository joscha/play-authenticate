organization := "com.feth"

name := "play-authenticate-simple-oauth"

scalaVersion := "2.11.2"

version := "1.0-SNAPSHOT"

val appDependencies = Seq(
  "com.feth" %% "play-authenticate" % "0.6.6-SNAPSHOT",
  javaCore,
  cache,
  javaWs,
  javaEbean
)

resolvers ++= Seq(
  "Apache" at "http://repo1.maven.org/maven2/",
  "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
  "play-easymail (release)" at "http://joscha.github.io/play-easymail/repo/releases/",
  "play-easymail (snapshot)" at "http://joscha.github.io/play-easymail/repo/snapshots/"
)

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    libraryDependencies ++= appDependencies
  )
