organization := "com.feth"

name := "play-authenticate"

javacOptions ++= Seq("-Werror")

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.5",
  "com.feth" %% "play-easymail" % "0.8.0-SNAPSHOT",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.apache.commons" % "commons-lang3" % "3.4",
  cache,
  javaWs
)

// add resolver for easymail snapshots
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val playAuthenticate = (project in file(".")).enablePlugins(PlayJava)
