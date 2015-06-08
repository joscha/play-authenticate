organization := "com.feth"

name := "play-authenticate"

scalaVersion := "2.11.6"

crossScalaVersions := Seq("2.10.5", "2.11.6")

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.4.1",
  "com.feth" %% "play-easymail" % "0.7.0",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.apache.commons" % "commons-lang3" % "3.4",
  javaCore,
  cache,
  javaWs
)

lazy val `play-authenticate` = (project in file("."))
  .enablePlugins(PlayJava)

fork in run := true