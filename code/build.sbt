organization := "com.feth"

name := "play-authenticate"

javacOptions ++= Seq("-Werror")

scalaVersion := "2.12.6"
crossScalaVersions := Seq("2.11.11", "2.12.6")

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.5",
  "com.feth" %% "play-easymail" % "0.9.3",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.apache.commons" % "commons-lang3" % "3.4",
  cacheApi,
  javaWs,
  openId,
  guice
)

// add resolver for easymail snapshots
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val playAuthenticate = (project in file(".")).enablePlugins(PlayJava)

releasePublishArtifactsAction := PgpKeys.publishSigned.value
