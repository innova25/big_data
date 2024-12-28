# Sử dụng Python base image
FROM python:3.9-slim

# Cài đặt các thư viện hệ thống cần thiết
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

# Cài đặt các thư viện Python yêu cầu
RUN pip install --upgrade pip
COPY requirements.txt .
RUN pip install -r requirements.txt

# Sao chép mã nguồn của ứng dụng vào container
COPY . /app

# Chuyển tới thư mục chứa ứng dụng
WORKDIR /app

# Mở cổng Flask app (5000)
EXPOSE 5000

# Lệnh để chạy ứng dụng Flask
CMD ["python", "app.py"]
