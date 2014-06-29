organization := "com.feth"

name := "play-authenticate"

scalaVersion := Option(System.getProperty("scala.version")).getOrElse("2.11.1")

version := "0.6.1-SNAPSHOT"

publishTo <<= (version) { version: String =>
  if (version.trim.endsWith("SNAPSHOT")) Some(Resolver.file("file",  new File( "../repo/snapshots" )))
  else                                   Some(Resolver.file("file",  new File( "../repo/releases" )))
}

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.3.4",
  "com.feth" %% "play-easymail" % "0.6.1-SNAPSHOT",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "commons-lang" % "commons-lang" % "2.6",
  javaCore,
  cache,
  javaWs
)

resolvers ++= Seq(
  "Apache" at "http://repo1.maven.org/maven2/",
  "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
  Resolver.url("play-easymail (release)", url("http://joscha.github.io/play-easymail/repo/releases/"))(Resolver.ivyStylePatterns),
  Resolver.url("play-easymail (snapshot)", url("http://joscha.github.io/play-easymail/repo/snapshots/"))(Resolver.ivyStylePatterns)
)

publishArtifact in packageDoc := false

lazy val root = (project in file(".")).enablePlugins(PlayJava)
