from flask import Flask, render_template, send_file
import pandas as pd
import matplotlib.pyplot as plt
from pyhive import hive
import io

app = Flask(__name__)

# Kết nối đến Hive và truy vấn dữ liệu phân cụm
def get_cluster_data():
    # Chuỗi kết nối SQLAlchemy
    conn = hive.Connection(host="spark-master", port=10000, database="ecom")
    query = """
        SELECT user_id, cluster, activity_frequency, unique_products, unique_brands, avg_spending
        FROM parquet.`hdfs://namenode:8020/cluster_out_put/clusters.parquet`
        """
    df = pd.read_sql(query, conn)

    # Giảm kích thước dữ liệu bằng cách sampling 5000 bản ghi
    df_sampled = df.sample(n=5000, random_state=42) if len(df) > 5000 else df
    return df_sampled

@app.route('/')
def index():
    # Render HTML cơ bản
    return render_template("index.html")

@app.route('/plot.png')
def plot_png():
    # Lấy dữ liệu phân cụm
    df = get_cluster_data()

    # Tạo biểu đồ phân tán bằng matplotlib
    fig, ax = plt.subplots(figsize=(10, 6))
    scatter = ax.scatter(
        df['activity_frequency'], df['avg_spending'], c=df['cluster'], cmap='viridis', alpha=0.6
    )
    ax.set_title("Clustering Visualization")
    ax.set_xlabel("Activity Frequency")
    ax.set_ylabel("Average Spending")
    plt.colorbar(scatter, ax=ax, label="Cluster")

    # Lưu biểu đồ vào buffer
    buf = io.BytesIO()
    plt.savefig(buf, format="png")
    buf.seek(0)
    plt.close(fig)

    # Trả về buffer dưới dạng ảnh PNG
    return send_file(buf, mimetype="image/png")

if __name__ == '__main__':
    app.run(debug=True, host="0.0.0.0")
