package job

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Main {

  def main(args: Array[String]): Unit = {
    // Tạo SparkSession
    val spark = SparkSession.builder()
      .appName("Kafka Stream to Single Daily Parquet File")
      .master("spark://spark-master:7077")
      .getOrCreate()

    // Đọc dữ liệu từ Kafka
    val kafkaSource = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "kafka1:8097,kafka2:8098,kafka3:8099")
      .option("subscribe", "ecommerce")
      .option("startingOffsets", "latest")
      .load()

    import spark.implicits._

    // Chuyển đổi dữ liệu từ Kafka sang định dạng JSON
    val parsedStream = kafkaSource.selectExpr("CAST(value AS STRING)").as[String]
      .selectExpr("value AS json_string")
      .selectExpr("from_json(json_string, 'event_time STRING, event_type STRING, product_id STRING, " +
        "category_id STRING, category_code STRING, brand STRING, price DOUBLE, user_id STRING, user_session STRING') AS data")
      .select("data.*")

    // Lấy ngày hiện tại theo định dạng yyyyMMdd
    val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val currentDate = LocalDate.now().format(dateFormatter)

    // Lưu dữ liệu vào một tệp duy nhất trong thư mục ngày
    val query = parsedStream.writeStream
      .format("parquet")
      .option("path", s"hdfs://namenode:8020/daily/$currentDate/data.parquet") // Lưu vào tệp với ngày yyyyMMdd
      .option("checkpointLocation", "hdfs://namenode:8020/checkpoint-spark-streaming-parquet") // Checkpoint để theo dõi tiến trình
      .trigger(Trigger.ProcessingTime("120 seconds")) // Micro-batch 30 giây
      .outputMode("append") // Ghi thêm vào tệp hiện có
      .start()

    query.awaitTermination()
  }
}
