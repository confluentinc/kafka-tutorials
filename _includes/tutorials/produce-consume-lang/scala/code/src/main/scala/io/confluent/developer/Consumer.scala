package io.confluent.developer

import io.confluent.developer.Configuration.ConsumerConf
import io.confluent.developer.schema.ScalaReflectionSerde.reflectionDeserializer4S
import io.confluent.developer.schema.{Book, ScalaReflectionSerde}
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.serialization.{Deserializer, Serdes}
import org.apache.kafka.common.utils.Bytes
import org.slf4j.{Logger, LoggerFactory}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import ujson.Obj

import scala.collection.mutable
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.jdk.DurationConverters._

object Consumer extends cask.MainRoutes with ScalaReflectionSerde with Configuration {

  val bookMap: mutable.Map[String, Book] = mutable.Map[String, Book]()

  private val consumerConf = ConfigSource.default.at("consumer").loadOrThrow[ConsumerConf]

  private val schemaRegistryConfigMap: Map[String, AnyRef] = Map[String, AnyRef](
    SCHEMA_REGISTRY_URL_CONFIG -> consumerConf.clientConfig.getString(SCHEMA_REGISTRY_URL_CONFIG)
  )

  override def port: Int = consumerConf.port
  override def host: String = consumerConf.host

  private def logger: Logger = LoggerFactory.getLogger(getClass)

  @cask.get("/count")
  def getCount(): Obj = {
    ujson.Obj("count" -> bookMap.size)
  }

  @cask.get("/books")
  def getBooks(): Obj = {
    ujson.Obj("results" -> ujson.Arr(
      bookMap.toArray.map { case (_: String, book: Book) =>
        upickle.default.writeJs(book)
      }: _*
    )
    )
  }

  def consume(consumer: KafkaConsumer[Bytes, Book]): Vector[Book] = {

    val books: ConsumerRecords[Bytes, Book] = consumer.poll((1 second) toJava)

    books.asScala.toVector.map(_.value())
  }

  new Thread(() => {

    logger debug "creating the deserializers and configuring"
    val bookDeserializer: Deserializer[Book] = reflectionDeserializer4S[Book]
    bookDeserializer.configure(schemaRegistryConfigMap.asJava, false)

    logger debug "creating the kafka consumer"
    val consumer = new KafkaConsumer[Bytes, Book](
      consumerConf.clientConfig.toProperties,
      Serdes.Bytes().deserializer(),
      bookDeserializer
    )

    consumer.subscribe(Vector(consumerConf.topics.bookTopic.name).asJava)

    sys.addShutdownHook {
      logger warn s"closing the kafka consumer"
      consumer.close((5 second) toJava)
    }

    while (true) {
      Thread.sleep((2 second) toMillis)

      logger debug s"polling the new events"
      val books: Vector[Book] = consume(consumer)

      if(books.nonEmpty) logger info s"just polled ${books.size} books from kafka"
      books.foreach { book =>
        bookMap += book.title -> book
      }
    }

  }).start()

  logger info s"starting the HTTP server"
  initialize()
}