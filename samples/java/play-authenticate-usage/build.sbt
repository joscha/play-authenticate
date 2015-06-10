organization := "com.feth"

name := "play-authenticate-usage"

scalaVersion := "2.11.6"

version := "1.0-SNAPSHOT"

val appDependencies = Seq(
  "be.objectify"  %% "deadbolt-java"     % "2.4.0-SNAPSHOT",
  "com.feth"      %% "play-authenticate" % "0.7.0-SNAPSHOT",
  "org.postgresql"    %  "postgresql"        % "9.4-1201-jdbc41",
  cache,
  javaWs,
  javaJdbc,
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.easytesting" % "fest-assert" % "1.4" % "test"
)

// add resolver for deadbolt and easymail snapshots
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val root = project.in(file("."))
  .enablePlugins(PlayJava, PlayEbean)
  .settings(
    libraryDependencies ++= appDependencies
  )
