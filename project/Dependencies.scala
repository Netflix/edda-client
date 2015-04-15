import sbt._

object Dependencies {
  object Versions {
    val scala     = "2.11.5"
    val slf4j     = "1.7.10"
    val spectator = "0.21.0"
    val iep       = "0.1.13"
    val rxjava    = "1.0.8"
    val rxnetty   = "0.4.8"
    val rxAwsSdk  = "0.1.1"
  }

  val archaiusCore    = "com.netflix.archaius" % "archaius-core" % "0.6.5"
  val awsObjectMapper = "com.netflix.awsobjectmapper" % "awsobjectmapper" % "1.9.16.2"
  val equalsVerifier  = "nl.jqno.equalsverifier" % "equalsverifier" % "1.5.1"
  val eureka          = "com.netflix.eureka" % "eureka-client" % "1.1.147"
  val governator      = "com.netflix.governator" % "governator" % "1.3.3"
  val guice           = "com.google.inject" % "guice" % "3.0"
  val jodaTime        = "joda-time" % "joda-time" % "2.5"
  val karyonAdmin     = "com.netflix.karyon2" % "karyon-admin-web" % "2.2.00-ALPHA7"
  val karyonCore      = "com.netflix.karyon2" % "karyon-core" % "2.2.00-ALPHA7"
  val iepConfig       = "com.netflix.iep" % "iep-config" % Versions.iep
  val iepNflxEnv      = "com.netflix.iep" % "iep-nflxenv" % Versions.iep
  val iepRxHttp       = "com.netflix.iep" % "iep-rxhttp" % Versions.iep
  val junit           = "junit" % "junit" % "4.10"
  val junitInterface  = "com.novocode" % "junit-interface" % "0.11"
  val jzlib           = "com.jcraft" % "jzlib" % "1.1.3"
  val rxAwsSdk        = "com.netflix.rx-aws-java-sdk" % "rx-aws-java-sdk" % Versions.rxAwsSdk
  val rxjava          = "io.reactivex" % "rxjava" % Versions.rxjava
  val rxnetty         = "io.reactivex" % "rxnetty" % Versions.rxnetty
  val rxnettyCtxts    = "io.reactivex" % "rxnetty-contexts" % Versions.rxnetty
  val rxnettySpectator= "io.reactivex" % "rxnetty-spectator" % Versions.rxnetty
  val scalaLibrary    = "org.scala-lang" % "scala-library" % Versions.scala
  val scalaLibraryAll = "org.scala-lang" % "scala-library-all" % Versions.scala
  val scalaLogging    = "com.typesafe.scala-logging" % "scala-logging_2.11" % "3.1.0"
  val scalaParsec     = "org.scala-lang.modules" % "scala-parser-combinators_2.11" % "1.0.2"
  val scalaReflect    = "org.scala-lang" % "scala-reflect" % Versions.scala
  val scalatest       = "org.scalatest" % "scalatest_2.11" % "2.2.1"
  val slf4jApi        = "org.slf4j" % "slf4j-api" % Versions.slf4j
  val spectatorApi    = "com.netflix.spectator" % "spectator-api" % Versions.spectator
  val spectatorNflx   = "com.netflix.spectator" % "spectator-nflx-plugin" % Versions.spectator
  val spectatorSandbox= "com.netflix.spectator" % "spectator-ext-sandbox" % Versions.spectator
}
