import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "play-authenticate-usage"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "be.objectify" 	%% "deadbolt-2"			% "1.1.3-SNAPSHOT",
      "com.feth"		%% "play-authenticate"	% "0.1.0-SNAPSHOT"
    )
    
//    val playAuthenticate = PlayProject(
//     "play-authenticate", "1.0-SNAPSHOT", Seq("org.mindrot" % "jbcrypt" % "0.3m","com.typesafe" %% "play-plugins-mailer" % "2.0.2"), mainLang = JAVA, path = file("modules/play-authenticate")
//    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
      resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/snapshots/"))(Resolver.ivyStylePatterns),
      //resolvers += "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/"
      resolvers += "Play Authenticate Repository" at "http://joscha.github.com/play-authenticate/repo/"
    )
    //.dependsOn(playAuthenticate)

}
