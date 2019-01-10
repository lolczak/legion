resolvers += Resolver.sbtPluginRepo("releases")
resolvers += Resolver.typesafeRepo("releases")

val catsVersion = "1.0.1"
val catsEffectVersion = "0.8"
val circeVersion = "0.9.0"
val scalaTestVersion = "3.0.5"
val testcontainersVersion = "0.13.0"
val logbackVersion = "1.2.3"
val slf4jVersion = "1.7.25"
val commonsLangVersion = "3.3.2"
val scalaLoggingVersion = "3.7.2"
val http4sVersion = "0.18.16"

val legionVersion = "0.1-SNAPSHOT"

lazy val `ipfs-client` = (project in file("ipfs-client"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-lang3" % commonsLangVersion,
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion exclude("org.scala-lang", "scala-reflect"),
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s" %% "http4s-client" % http4sVersion
    ) ++ testing ++ functional 
  )

lazy val `ipfs-oplog` = (project in file("ipfs-oplog"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-lang3" % commonsLangVersion,
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion exclude("org.scala-lang", "scala-reflect"),
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion
    ) ++ testing ++ functional 
  )
  .dependsOn(`ipfs-client`)

lazy val `legion-core` = (project in file("legion-core"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-lang3" % commonsLangVersion,
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion exclude("org.scala-lang", "scala-reflect"),
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion
    ) ++ testing ++ functional 
  )

lazy val `legion-p2p` = (project in file("legion-p2p"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-lang3" % commonsLangVersion,
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion exclude("org.scala-lang", "scala-reflect"),
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion
    ) ++ testing ++ functional 
  )

lazy val `legion-crdt` = (project in file("legion-crdt"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-lang3" % commonsLangVersion,
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion exclude("org.scala-lang", "scala-reflect"),
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion
    ) ++ testing ++ functional 
  )

lazy val commonSettings = Seq(
  scalaVersion := "2.12.8",
  organization := "io.rebelapps.legion",
  version := legionVersion,
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    "io.circe" %% "circe-java8" % circeVersion
  )
)

lazy val functional = Seq(
  "com.chuusai" %% "shapeless" % "2.3.3",
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,
)
lazy val circe = Seq(
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
)
lazy val testing = Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "org.scalacheck" %% "scalacheck" % "1.13.5" % Test
)

