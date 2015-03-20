organization := "com.feth"

name := "play-authenticate"

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.10.4", "2.11.5")

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.3.6",
  "com.feth" %% "play-easymail" % "0.7.0",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "commons-lang" % "commons-lang" % "2.6",
  javaCore,
  cache,
  javaWs
)

lazy val root = (project in file(".")).enablePlugins(PlayJava)

fork in run := true