import play.PlayJava

name := "play-authenticate-usage"

scalaVersion := "2.11.1"

version := "1.0-SNAPSHOT"

val appDependencies = Seq(
  "be.objectify"  %% "deadbolt-java"     % "2.3.0-RC1",
  // Comment the next line for local development of the Play Authentication core:
  "com.feth"      %% "play-authenticate" % "0.6.5-SNAPSHOT",
  "postgresql"    %  "postgresql"        % "9.1-901-1.jdbc4",
  javaCore,
  cache,
  javaWs,
  javaJdbc,
  javaEbean,
  "org.webjars" %% "webjars-play" % "2.3.0",
  "org.webjars" % "bootstrap" % "3.1.1-2",
  "org.webjars" % "jquery" % "1.8.3"
)

resolvers ++= Seq(
  "Apache" at "http://repo1.maven.org/maven2/",
  "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
  "play-easymail (release)" at "http://joscha.github.io/play-easymail/repo/releases/",
  "play-easymail (snapshot)" at "http://joscha.github.io/play-easymail/repo/snapshots/",
  Resolver.url("Objectify Play Repository", url("http://schaloner.github.io/releases/"))(Resolver.ivyStylePatterns),
  "play-authenticate (release)" at "http://joscha.github.io/play-authenticate/repo/releases/",
  "play-authenticate (snapshot)" at "http://joscha.github.io/play-authenticate/repo/snapshots/"
)

//  Uncomment the next line for local development of the Play Authenticate core:
//lazy val playAuthenticate = project.in(file("modules/play-authenticate")).enablePlugins(PlayJava)

lazy val root = project.in(file("."))
  .enablePlugins(PlayJava)
  .settings(
    libraryDependencies ++= appDependencies
  )
  //  Uncomment the next lines for local development of the Play Authenticate core:
  //.dependsOn(playAuthenticate)
  //.aggregate(playAuthenticate)
