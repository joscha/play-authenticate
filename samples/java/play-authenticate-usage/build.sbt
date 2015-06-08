import play.PlayJava

name := "play-authenticate-usage"

scalaVersion := "2.11.6"

version := "1.0-SNAPSHOT"

val appDependencies = Seq(
  "be.objectify" %% "deadbolt-java" % "2.4.0",
  // Comment the next line for local development of the Play Authentication core:
  "com.feth"      %% "play-authenticate" % "0.7.0",
  "org.postgresql"    %  "postgresql"        % "9.4-1201-jdbc41",
  javaCore,
  cache,
  javaWs,
  "com.typesafe.play" % "play-jdbc_2.11" % "2.4.0-M3",
  "org.webjars" %% "webjars-play" % "2.4.0-SNAPSHOT",
  "org.webjars" % "bootstrap" % "3.3.4"
)

resolvers += Resolver.url("Objectify Play Repository", url("http://deadbolt.ws/releases/"))(Resolver.ivyStylePatterns)

//  Uncomment the next line for local development of the Play Authenticate core:
//lazy val playAuthenticate = project.in(file("modules/play-authenticate")).enablePlugins(PlayJava)

/* Uncomment the next lines for local development of the Play Authenticate core: */
//.dependsOn(playAuthenticate)
//.aggregate(playAuthenticate)
