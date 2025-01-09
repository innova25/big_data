
from kafka import KafkaConsumer, KafkaProducer

consumer = KafkaConsumer(
    'ecommerce',
    bootstrap_servers='localhost:8097,localhost:8098,localhost:8099',
    auto_offset_reset='earliest', 
    enable_auto_commit=True,
    group_id='ecommerce_group',
    value_deserializer=lambda x: x.decode('utf-8')
)

print("Consumer connected. Waiting for messages...")

for message in consumer:
    print(f"Received message: {message.value}")
