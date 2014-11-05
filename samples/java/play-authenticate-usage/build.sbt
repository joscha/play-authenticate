import play.PlayJava

name := "play-authenticate-usage"

scalaVersion := "2.11.2"

version := "1.0-SNAPSHOT"

val appDependencies = Seq(
  "be.objectify"  %% "deadbolt-java"     % "2.3.2",
  // Comment the next line for local development of the Play Authentication core:
  "com.feth"      %% "play-authenticate" % "0.6.7",
  "postgresql"    %  "postgresql"        % "9.1-901-1.jdbc4",
  javaCore,
  cache,
  javaWs,
  javaJdbc,
  javaEbean,
  "org.webjars" %% "webjars-play" % "2.3.0",
  "org.webjars" % "bootstrap" % "3.2.0"
)

resolvers ++= Seq(
  "Apache" at "http://repo1.maven.org/maven2/",
  "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/"
)

//  Uncomment the next line for local development of the Play Authenticate core:
//lazy val playAuthenticate = project.in(file("modules/play-authenticate")).enablePlugins(PlayJava)

lazy val root = project.in(file("."))
  .enablePlugins(PlayJava)
  .settings(
    libraryDependencies ++= appDependencies
  )
  /* Uncomment the next lines for local development of the Play Authenticate core: */
  //.dependsOn(playAuthenticate)
  //.aggregate(playAuthenticate)
