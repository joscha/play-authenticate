organization := "com.feth"

name := "play-authenticate-simple-oauth"

scalaVersion := "2.11.1"

version := "1.0-SNAPSHOT"

val appDependencies = Seq(
  "com.feth" %% "play-authenticate" % "0.6.0-SNAPSHOT",
  javaCore,
  cache,
  javaWs,
  javaEbean
)

resolvers ++= Seq(
  "Apache" at "http://repo1.maven.org/maven2/",
  "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
  Resolver.url("play-easymail (release)", url("http://joscha.github.io/play-easymail/repo/releases/"))(Resolver.ivyStylePatterns),
  Resolver.url("play-easymail (snapshot)", url("http://joscha.github.io/play-easymail/repo/snapshots/"))(Resolver.ivyStylePatterns),
  Resolver.url("play-authenticate (release)", url("http://joscha.github.io/play-authenticate/repo/releases/"))(Resolver.ivyStylePatterns),
  Resolver.url("play-authenticate (snapshot)", url("http://joscha.github.io/play-authenticate/repo/snapshots/"))(Resolver.ivyStylePatterns)
)

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    libraryDependencies ++= appDependencies
  )
