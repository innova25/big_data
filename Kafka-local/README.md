Hướng dẫn cài đặt Kafka bằng Docker trên Windows
Bước 1: Cài đặt Docker Desktop

Tải Docker Desktop cho Windows từ trang web chính thức: https://www.docker.com/products/docker-desktop/
Cài đặt Docker Desktop bằng cách làm theo hướng dẫn trên màn hình.
Khởi động Docker Desktop sau khi cài đặt xong.

Bước 2:  Chạy Docker Compose
Mở terminal(trong file yarn), chạy lệnh sau:
docker compose up -d


Bước 3: Mở terminal trong container kafka1, chạy lệnh sau:

bash

kafka-topics.sh --create \
  --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 \
  --replication-factor 3 \
  --partitions 3 \
  --topic ecommerce

Lệnh này sẽ tạo ra 1 topic mới tên là ecommerce.

Bước 4: Mở thêm 2 cửa sổ terminal khác nhau, chạy song song 2 lệnh:

bash

kafka-console-producer --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 --topic ecommerce
<!-- first terminal -->

bash

kafka-console-consumer --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 --topic ecommerce --from-beginning
<!-- second terminal -->

2 lệnh trên sẽ tạo ra Producer và Consumer cho topic ecommerce, và bắt đầu có thể gửi thông điệp cho nhau qua topic.

Sau khi cài đặt thành công Kafka topic và khởi tạo được Producer-Consumer, tiếp theo chạy lần lượt 2 file product_store.py để tạo file product_cache.pkl, data_generator.py để khởi tạo streaming data và lưu thẳng vào topic name "ecommerce".
