# Platform Setup Guide

## Setup Platform
Run the following command to set up the platform:
```bash
docker compose up -d
```

## Setup Batch Data

### Step 1: Upload Data to HDFS
Use the following command to upload your data file to HDFS:
```bash
hdfs dfs -put {file} hdfs://namenode:8020/raw/wait_to_combine/
```

### Step 2: Run Spark-Shell
1. Start `spark-shell` and execute the following commands:

```scala
val csvPath = "hdfs://namenode:8020/raw/wait_to_combine/{monthly csv file}"
val df = spark.read.option("header", "true").option("inferSchema", "true").csv(csvPath)

df.show(5)

val parquetPath = "hdfs://namenode:8020/raw/all.parquet"
df.write.mode("append").parquet(parquetPath)
```

## Setup Superset Visualization

### Step 1: Import Dashboard
Download and import the dashboard from the following link:
[Dashboard Link](https://drive.google.com/drive/u/0/folders/1meYCbYKMbTmV2aIoOXX8a4JP5MGTrnEa)

### Step 2: Run ThriftServer
Run the following command to start ThriftServer:
```bash
start-thriftserver.sh \
    --master spark://spark-master:7077 \
    --conf spark.sql.catalogImplementation=hive \
    --conf spark.cores.max=10 \
    --executor-memory 2G
```

## Setup Druid

### Step 1: Import Supervisor
Download and import the supervisor configuration from the following link:
[Supervisor Link](https://drive.google.com/drive/u/0/folders/1meYCbYKMbTmV2aIoOXX8a4JP5MGTrnEa)

## Run Spark Applications

### Step 1: Build Application
Run the following command in `sbt shell`:
```bash
assembly
```

### Step 2: Move JAR File
Move the generated JAR file to HDFS or the Spark master container.

### Step 3: Run Spark Jobs
#### Streaming Job
Use the following command to submit a streaming job:
```bash
spark-submit \
    --master spark://spark-master:7077 \
    --deploy-mode client \
    --class job.Main \
    --packages org.apache.spark:spark-streaming-kafka-0-10_2.13:3.5.2,org.apache.kafka:kafka-clients:3.5.2 \
    --executor-memory 2G \
    --executor-core 5 {path to jar}
```

#### Cluster Job
Use the following command to submit a cluster job:
```bash
spark-submit \
    --master spark://spark-master:7077 \
    --deploy-mode client \
    --class job.Clustering \
    --packages org.apache.spark:spark-sql_2.12:3.5.2,org.apache.spark:spark-mllib_2.12:3.5.2 \
    --executor-cores 5 \
    --executor-memory 2G \
    --conf spark.cores.max=10 \
    {path to jar}
```

#### Daily Job
Follow the above steps and configure appropriately for daily job requirements.
