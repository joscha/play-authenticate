useGpg := true

// the following lines are being ignored as long as we use the GPG binary
val isTravisRun = System.getProperty("travis") == "1"

val pgpPass = ("" + System.getProperty("PGP_PASSPHRASE")).toCharArray

pgpPassphrase := {
  if (!isSnapshot.value && !isTravisRun)
    Some(pgpPass)
  else
    Some(Array())
}
