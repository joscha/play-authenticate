organization := "com.feth"

name := "play-authenticate-simple-oauth"

scalaVersion := "2.11.8"

version := "1.0-SNAPSHOT"

val appDependencies = Seq(
  // Comment the next line for local development of the Play Authentication core:
  "com.feth" %% "play-authenticate" % "0.8.3",
  cache,
  javaWs
)

// add resolver for easymail snapshots
resolvers += Resolver.sonatypeRepo("snapshots")

//  Uncomment the next line for local development of the Play Authenticate core:
//lazy val playAuthenticate = project.in(file("modules/play-authenticate")).enablePlugins(PlayJava)

lazy val root = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)
  .settings(
    libraryDependencies ++= appDependencies
  )
  /* Uncomment the next lines for local development of the Play Authenticate core: */
  //.dependsOn(playAuthenticate)
  //.aggregate(playAuthenticate)
