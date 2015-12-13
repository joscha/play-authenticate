import sbtrelease.ReleasePlugin._
import ReleaseTransformations._
import com.typesafe.sbt.pgp.PgpKeys._


javacOptions ++= {
  if (System.getProperty("rt.path") != null)  Seq("-source", "1.8", "-target", "1.8", "-bootclasspath", System.getProperty("rt.path"))
  else                                        Seq()
}

releasePublishArtifactsAction := PgpKeys.publishSigned.value

releaseCrossBuild := true
