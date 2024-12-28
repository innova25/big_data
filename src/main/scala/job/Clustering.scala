package job

import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.feature.{VectorAssembler, StandardScaler}
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.sql.functions._

object Clustering {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Clustering Users")
      .master("spark://spark-master:7077")
      .getOrCreate()

    val parquetPath = "hdfs://namenode:8020/raw/all.parquet"
    val rawData = spark.read.parquet(parquetPath)

    val processedData = rawData.filter(col("event_type") === "purchase")
      .groupBy("user_id")
      .agg(
        count("event_type").as("activity_frequency"),
        countDistinct("product_id").as("unique_products"),
        countDistinct("brand").as("unique_brands"),
        sum("price").as("total_spending")
      )
      .withColumn("avg_spending", col("total_spending") / col("activity_frequency"))

    val assembler = new VectorAssembler()
      .setInputCols(Array("activity_frequency", "unique_products", "unique_brands", "avg_spending"))
      .setOutputCol("features")

    val featureData = assembler.transform(processedData)

    val scaler = new StandardScaler()
      .setInputCol("features")
      .setOutputCol("scaledFeatures")
      .fit(featureData)

    val scaledData = scaler.transform(featureData)

    val kmeans = new KMeans()
      .setK(4)
      .setSeed(1)
      .setFeaturesCol("scaledFeatures")
      .setPredictionCol("cluster")

    val model = kmeans.fit(scaledData)

    val clusteredData = model.transform(scaledData)

    val outputPath = "hdfs://namenode:8020/cluster_out_put/clusters.parquet"
    clusteredData
      .select("user_id", "cluster", "activity_frequency", "unique_products", "unique_brands", "avg_spending")
      .write
      .mode("overwrite")
      .parquet(outputPath)

    // Lưu lại mô hình và scaler
    val modelPath = "hdfs://namenode:8020/model/kmeans"
    val scalerPath = "hdfs://namenode:8020/model/scaler"
    model.write.overwrite().save(modelPath)
    scaler.write.overwrite().save(scalerPath)

    spark.stop()
  }
}
