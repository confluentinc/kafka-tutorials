import sbt.Attributed
import sbt.fullRunTask
import complete.DefaultParsers
import Dependencies._
import de.gccc.jib.JibPlugin.autoImport.{jibDockerBuild, jibRegistry}
import sbt.Keys.commands

ThisBuild / scalaVersion := "2.13.2"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "io.confluent.developer"
ThisBuild / organizationName := "confluent"

ThisBuild / javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

ThisBuild / scalacOptions ++= Seq("-language:postfixOps")

def dockerPackage(command: String, settings: Seq[Def.Setting[_]]): Command = Command.command(command) { state =>
  val settingsState = (Project extract state).appendWithoutSession(settings, state)
  val (commandState, _) = Project.extract(settingsState).runTask(jibDockerBuild, settingsState)
  commandState
}

lazy val root = (project in file("."))
  .settings(
    name := "produce-consume-scala",
    resolvers += "Confluent Repo" at "http://packages.confluent.io/maven",
    jibRegistry := "",
    libraryDependencies ++= katancsv ++: (caskHttp ::
      avro4S ::
      logback ::
      pureConfig ::
      kafkaClients ::
      confluentSerde ::
      scalaTest % Test ::
      kafkaTestcontainers % Test :: Nil),

    commands += dockerPackage("packageConsumer", Seq(
      jibName := "scala-consumer",
      mainClass in Compile := Some("io.confluent.developer.Consumer")
    )),

    commands += dockerPackage("packageProducer", Seq(
      jibName := "scala-producer",
      mainClass in Compile := Some("io.confluent.developer.Producer")
    ))
  )

lazy val produce: InputKey[Unit] = inputKey[Unit]("Message Production")
produce := (runner in Compile).value.run("io.confluent.developer.Producer",
  Attributed.data((fullClasspath in Compile).value),
  DefaultParsers.spaceDelimited("arguments").parsed,
  streams.value.log
)

lazy val consume: TaskKey[Unit] = taskKey[Unit]("Message Consumption")
fullRunTask(consume, Compile, "io.confluent.developer.Consumer")

lazy val topicCreation: TaskKey[Unit] = taskKey[Unit]("Topic creation task")
fullRunTask(topicCreation, Compile, "io.confluent.developer.helper.TopicCreation")

lazy val schemaPublication: TaskKey[Unit] = taskKey[Unit]("Schema publication task")
fullRunTask(schemaPublication, Compile, "io.confluent.developer.helper.SchemaPublication")