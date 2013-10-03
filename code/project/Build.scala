import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "play-authenticate"
    val appVersion      = "0.3.4-SNAPSHOT"

    val appDependencies = Seq(
        javaCore,
        cache
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      organization := "com.feth",
      resolvers += "Apache" at "http://repo1.maven.org/maven2/",
      resolvers += "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",

      resolvers += Resolver.url("play-easymail (release)", url("http://repo.laf.su/releases/"))(Resolver.ivyStylePatterns),
      resolvers += Resolver.url("play-easymail (snapshot)", url("http://repo.laf.su/snapshots/"))(Resolver.ivyStylePatterns),

      libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.3",
      libraryDependencies += "com.feth" %% "play-easymail" % "0.3-SNAPSHOT",
      libraryDependencies += "org.mindrot" % "jbcrypt" % "0.3m",
      libraryDependencies += "commons-lang" % "commons-lang" % "2.6"
    )
}
