publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

homepage := Some(url("http://joscha.github.io/play-authenticate"))

licenses := Seq("Apache 2" -> url("http://opensource.org/licenses/Apache-2.0"))

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <scm>
    <url>git@github.com:joscha/play-authenticate.git</url>
    <connection>scm:git@github.com:joscha/play-authenticate.git</connection>
  </scm>
  <developers>
    <developer>
      <id>joscha</id>
      <name>Joscha Feth</name>
      <url>http://www.feth.com</url>
    </developer>
  </developers>)
