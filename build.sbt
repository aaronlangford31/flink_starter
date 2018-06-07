ThisBuild / resolvers ++= Seq(
  "Apache Development Snapshot Repository".at(
    "https://repository.apache.org/content/repositories/snapshots/"),
  Resolver.mavenLocal
)

name := "flink_starter"

version := "0.1-SNAPSHOT"

organization := "com.instructure"

ThisBuild / scalaVersion := "2.11.12"

val flinkVersion = "1.5.0"

val avroDependencies = Seq(
  "com.sksamuel.avro4s" %% "avro4s-core" % "1.9.0"
)

val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-connector-kinesis" % "1.5-SNAPSHOT",
  "org.apache.flink" %% "flink-scala"             % flinkVersion % "provided",
  "org.apache.flink" %% "flink-streaming-scala"   % flinkVersion % "provided",
  "org.apache.flink" %% "flink-table"             % flinkVersion,
  "org.apache.flink" %% "flink-test-utils"        % flinkVersion
)

val testDependencies = Seq(
  "org.apache.flink" %% "flink-test-utils" % flinkVersion % "test",
  "org.scalatest"    %% "scalatest"        % "3.0.5"      % "test"
)

lazy val root = (project in file(".")).settings(
  libraryDependencies ++= (avroDependencies ++ flinkDependencies ++ testDependencies)
)

assembly / mainClass := Some("com.instructure.pandata.flink_starter.ProcessTaxiData")

sourceGenerators in Compile += (avroScalaGenerate in Compile).taskValue

// make run command include the provided dependencies
Compile / run := Defaults
  .runTask(Compile / fullClasspath, Compile / run / mainClass, Compile / run / runner)
  .evaluated

// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true

// exclude Scala library from assembly
assembly / assemblyOption := (assembly / assemblyOption).value.copy(includeScala = false)

assemblyMergeStrategy in assembly := {
  case "log4j.properties"                                          => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
