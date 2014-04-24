import play.PlayImport._

import PlayKeys._

organization := "com.feth"

name := "play-authenticate"

version := "0.5.3-RC1"

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.3",
  "com.feth" %% "play-easymail" % "0.5-SNAPSHOT",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "commons-lang" % "commons-lang" % "2.6",
  javaCore,
  cache,
  javaWs
)

resolvers ++= Seq(
  "Apache" at "http://repo1.maven.org/maven2/",
  "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
  Resolver.url("play-easymail (release)", url("http://joscha.github.com/play-easymail/repo/releases/"))(Resolver.ivyStylePatterns),
  Resolver.url("play-easymail (snapshot)", url("http://joscha.github.com/play-easymail/repo/snapshots/"))(Resolver.ivyStylePatterns)
)

lazy val root = (project in file(".")).addPlugins(PlayJava)
