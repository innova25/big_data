
FROM apache/superset:latest

USER root
RUN pip install psycopg2-binary pyhive thrift thrift_sasl

USER superset
