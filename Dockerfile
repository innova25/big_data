FROM bitnami/spark:3.5.1
COPY ./yarn_config/core-site.xml /opt/bitnami/spark/conf
COPY ./yarn_config/yarn-site.xml /opt/bitnami/spark/conf
COPY ./yarn_config/hdfs-site.xml /opt/bitnami/spark/conf
