organization := "com.feth"

name := "play-authenticate"

scalaVersion := "2.11.2"

crossScalaVersions := Seq("2.10.4", "2.11.2")

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.3.5",
  "com.feth" %% "play-easymail" % "0.6.7",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "commons-lang" % "commons-lang" % "2.6",
  javaCore,
  cache,
  javaWs
)

lazy val root = (project in file(".")).enablePlugins(PlayJava)
