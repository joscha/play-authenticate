import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "play-authenticate-tests"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      javaCore
    )

    val playAuthenticate = play.Project(
      "play-authenticate", "1.0-SNAPSHOT", Seq(javaCore, cache), path = file("modules/play-authenticate")
    ).settings(
      libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.2",
      libraryDependencies += "com.feth" %% "play-easymail" % "0.5-SNAPSHOT",
      libraryDependencies += "org.mindrot" % "jbcrypt" % "0.3m",
      libraryDependencies += "commons-lang" % "commons-lang" % "2.6",

      resolvers += Resolver.url("play-easymail (release)", url("http://joscha.github.com/play-easymail/repo/releases/"))(Resolver.ivyStylePatterns),
      resolvers += Resolver.url("play-easymail (snapshot)", url("http://joscha.github.com/play-easymail/repo/snapshots/"))(Resolver.ivyStylePatterns)
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(

      resolvers += Resolver.url("Objectify Play Repository (release)", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
      resolvers += Resolver.url("Objectify Play Repository (snapshot)", url("http://schaloner.github.com/snapshots/"))(Resolver.ivyStylePatterns),

      resolvers += Resolver.url("play-easymail (release)", url("http://joscha.github.com/play-easymail/repo/releases/"))(Resolver.ivyStylePatterns),
      resolvers += Resolver.url("play-easymail (snapshot)", url("http://joscha.github.com/play-easymail/repo/snapshots/"))(Resolver.ivyStylePatterns),

      resolvers += Resolver.url("play-authenticate (release)", url("http://joscha.github.com/play-authenticate/repo/releases/"))(Resolver.ivyStylePatterns),
      resolvers += Resolver.url("play-authenticate (snapshot)", url("http://joscha.github.com/play-authenticate/repo/snapshots/"))(Resolver.ivyStylePatterns)
    ).dependsOn(playAuthenticate).aggregate(playAuthenticate)

}
