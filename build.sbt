name := "vertx"

version := "0.1"

scalaVersion := "2.12.8"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

val Vertx = "3.5.0"

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.google.guava" % "guava" % "27.1-jre",
  "org.apache.commons" % "commons-lang3" % "3.8.1",
  "com.twitter" %% "finagle-http" % "19.5.1",

  "io.vertx" % "vertx-codegen" % Vertx % Provided,
  "io.vertx" %% "vertx-lang-scala"  % Vertx,
  "io.vertx" % "vertx-hazelcast" % Vertx
)
