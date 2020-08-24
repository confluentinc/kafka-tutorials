import sbt._

object Dependencies {

  // tutorial essentials
  lazy val kafkaClients: ModuleID = "org.apache.kafka" % "kafka-clients" % "2.5.0"
  lazy val confluentSerde: ModuleID = "io.confluent" % "kafka-streams-avro-serde" % "5.5.0"
  lazy val avro4S: ModuleID = "com.sksamuel.avro4s" %% "avro4s-core" % "3.1.0"
  lazy val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % "3.1.1"
  lazy val kafkaTestcontainers = "org.testcontainers" % "kafka" % "1.14.3"


  // presentation boiler plate
  lazy val logback: ModuleID = "ch.qos.logback" % "logback-classic" % "1.2.3"
  lazy val pureConfig: ModuleID = "com.github.pureconfig" %% "pureconfig" % "0.12.3"
  lazy val caskHttp: ModuleID = "com.lihaoyi" %% "cask" % "0.6.7"
  lazy val katancsv: Seq[ModuleID] =
    "com.nrinaudo" %% "kantan.csv-generic" % "0.6.1" ::
    "com.nrinaudo" %% "kantan.csv-java8" % "0.6.1" ::
    "com.nrinaudo" %% "kantan.csv-enumeratum" % "0.6.1" :: Nil

}
