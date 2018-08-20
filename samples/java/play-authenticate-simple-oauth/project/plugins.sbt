// Comment to get more information during initialisation
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % Option(System.getProperty("play.version")).getOrElse("2.6.12"))

addSbtPlugin("com.typesafe.sbt" % "sbt-play-ebean" % "4.0.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-play-enhancer" % "1.1.0")

// Uncomment the next line for local development of the Play Authentication core:
//addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")

// Uncomment the next line for local development of the Play Authentication core:
//addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.7")
