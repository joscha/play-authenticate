import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "play-authenticate"
    val appVersion      = "0.1.7-SNAPSHOT"

    val appDependencies = Seq(
      "org.apache.httpcomponents" % "httpclient" % "4.2",
      "com.feth" %% "play-easymail" % "0.1-SNAPSHOT",
      "org.mindrot" % "jbcrypt" % "0.3m"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      organization := "com.feth",
      resolvers += "Apache" at "http://repo1.maven.org/maven2/",
      resolvers += "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/"
    )
}