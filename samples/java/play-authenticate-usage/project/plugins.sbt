// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % Option(System.getProperty("play.version")).getOrElse("2.6.1"))

addSbtPlugin("com.typesafe.sbt" %% "sbt-play-ebean" % "4.0.2")

// TODO: find a way to automatically load sbt plugins of projects we depend on
// if you see this and know how to do it, please open a pull request :)

// Uncomment the next line for local development of the Play Authentication core:
//addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

// Uncomment the next line for local development of the Play Authentication core:
//addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-play-enhancer" % "1.1.0")

// this is helpful plugin for figuring out dependencies
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.0")

addSbtPlugin("com.heroku" % "sbt-heroku" % "1.0.1")
