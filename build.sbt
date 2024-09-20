ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.18"

lazy val root = (project in file("."))
  .settings(
    assembly / mainClass := Some("Main"),
    name := "yarn",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "3.5.2",
      "org.apache.spark" %% "spark-streaming" % "3.5.2",
      "org.apache.spark" %% "spark-sql" % "3.5.2",
      "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.5.2",
      "org.apache.kafka" % "kafka-clients" % "3.5.2"
    )
  )

ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _                        => MergeStrategy.first
}