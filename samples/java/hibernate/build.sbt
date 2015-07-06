organization := "com.feth"

name := "play-authenticate-hibernate"

scalaVersion := "2.11.6"

version := "1.0-SNAPSHOT"

PlayKeys.externalizeResources := true

val appDependencies = Seq(
  javaJpa,
  "be.objectify"  %% "deadbolt-java"     % "2.4.0",
  // Comment the next line for local development of the Play Authentication core:
  "com.feth"      %% "play-authenticate" % "0.7.0-SNAPSHOT",
  //"org.postgresql"    %  "postgresql"        % "9.4-1201-jdbc41",
  "org.hibernate" % "hibernate-entitymanager" % "4.3.10.Final",
  "mysql" % "mysql-connector-java" % "5.1.36",
  javaJdbc,
  cache,
  javaWs,
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.easytesting" % "fest-assert" % "1.4" % "test"
)

// add resolver for deadbolt and easymail snapshots
resolvers += Resolver.sonatypeRepo("snapshots")

routesGenerator := InjectedRoutesGenerator

//  Uncomment the next line for local development of the Play Authenticate core:
//lazy val playAuthenticate = project.in(file("modules/play-authenticate")).enablePlugins(PlayJava)

lazy val root = project.in(file(".")).enablePlugins(PlayJava).settings(libraryDependencies ++= appDependencies)

  /* Uncomment the next lines for local development of the Play Authenticate core: */
  //.dependsOn(playAuthenticate)
  //.aggregate(playAuthenticate)
