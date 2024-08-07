package job

import org.apache.spark.sql.SparkSession

object Main {
  def main(args: Array[String]): Unit = {
    val spark : SparkSession = SparkSession.builder().master("yarn").appName("test").getOrCreate()
    val sc = spark.sparkContext
    val textFile = sc.textFile("hdfs://namenode/test/wordcount.txt")
    val counts = textFile.flatMap(line => line.split(" "))
      .map(word => (word, 1))
      .reduceByKey(_ + _)
    counts.saveAsTextFile("hdfs://namenode/output")
  }
}