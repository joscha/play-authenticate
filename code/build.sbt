organization := "com.feth"

name := "play-authenticate"

scalaVersion := "2.11.2"

crossScalaVersions := Seq("2.10.4", "2.11.2")

version := "0.6.7-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.3.5",
  "com.feth" %% "play-easymail" % "0.6.6-SNAPSHOT",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "commons-lang" % "commons-lang" % "2.6",
  javaCore,
  cache,
  javaWs
)

resolvers ++= Seq(
  "Apache" at "http://repo1.maven.org/maven2/",
  "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
  "play-easymail (release)" at "http://joscha.github.io/play-easymail/repo/releases/",
  "play-easymail (snapshot)" at "http://joscha.github.io/play-easymail/repo/snapshots/"
)

lazy val root = (project in file(".")).enablePlugins(PlayJava)
