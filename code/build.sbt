organization := "com.feth"

name := "play-authenticate"

scalaVersion := "2.11.6"

crossScalaVersions := Seq("2.11.6")

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.5",
  "com.feth" %% "play-easymail" % "0.7.0-SNAPSHOT",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.apache.commons" % "commons-lang3" % "3.4",
  cache,
  javaWs
)

// add resolver for easymail snapshots
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val root = (project in file(".")).enablePlugins(PlayJava)
