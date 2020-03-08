name := "legion"
organization := "me.olczak"
scalaVersion := "2.13.1"

val catsVersion       = "2.1.1"
val shapelessVersion  = "2.3.3"
val scalaTestVersion  = "3.1.1"
val scalaCheckVersion = "1.14.0"

lazy val testLibs = Seq(
  "org.scalatest"  %% "scalatest"  % scalaTestVersion  % Test,
  "org.scalacheck" %% "scalacheck" % scalaCheckVersion % Test
)

libraryDependencies ++= Seq(
  "org.typelevel"        %% "cats-core"   % catsVersion,
  "com.chuusai"          %% "shapeless"   % shapelessVersion,
  "com.typesafe.akka"    %% "akka-actor"  % "2.6.3",
  "com.typesafe.akka"    %% "akka-remote" % "2.6.3",
  "org.apache.zookeeper" % "zookeeper"    % "3.6.0",
  "dev.zio"              %% "zio"         % "1.0.0-RC18-1"
) ++ testLibs

lazy val legion = project in file(".")

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)

scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-encoding",
  "utf8",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps",
  "-Xfatal-warnings"
)
