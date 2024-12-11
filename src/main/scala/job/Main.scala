package job
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.kafka010.ConsumerStrategies._
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies._
import org.apache.spark.streaming.{Seconds, StreamingContext, Time}

object Main {

  private val checkpointDir = "hdfs://namenode:8020/checkpoint-spark-streaming-reduce"

  def main(args: Array[String]): Unit = {

    val streamingContext = StreamingContext.getOrCreate(checkpointDir, createStreamingContext)

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "kafka1:8097, kafka2:8098, kafka3:8099",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "spark_off_network_call",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("intra_off_call")

    val stream = KafkaUtils.createDirectStream[String, String](
      streamingContext,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    val lines = stream.map(record => record.value)

    lines.foreachRDD((rdd: RDD[String], time: Time) => {
      if (!rdd.isEmpty()) {
        val hdfsPath = s"hdfs://namenode:8020/intra-network/output_${time.milliseconds}"
        rdd.saveAsTextFile(hdfsPath)
      }
    })


    streamingContext.start()
    streamingContext.awaitTermination()
  }

  private def createStreamingContext(): StreamingContext = {
    val sparkConfig = new SparkConf().setMaster("spark://spark-master:7077").setAppName("Intra-network stream")
    val streamingContext = new StreamingContext(sparkConfig, Seconds(30))
    streamingContext.checkpoint(checkpointDir)
    streamingContext
  }
}