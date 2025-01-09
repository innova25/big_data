import redis
from datetime import datetime, timedelta
import json

# Kết nối tới Redis
redis_client = redis.StrictRedis(host='localhost', port=6379, db=0)

# Ngăn chặn các request chứa các từ khóa SQL
def detect_sql_injection(request):
    sql_keywords = ['SELECT', 'INSERT', 'DELETE', 'UPDATE', 'DROP']
    for keyword in sql_keywords:
        if keyword in request.upper():
            return True
    return False

# Thêm user_id vào blackList trong một khoảng thời gian nhất định
def add_to_blacklist(user_id, duration_minutes=60):
    redis_client.setex(f"blacklist:{user_id}", timedelta(minutes=duration_minutes), "blocked")

# Kiểm tra xem user_id có nằm trong blackList hay không
def is_in_blacklist(user_id):
    return redis_client.exists(f"blacklist:{user_id}")

# Ngăn chặn hành vi mua một mặt hàng quá nhiều trong một khoảng thời gian
def detect_large_order(user_id, product_id, max_quantity, time_window_minutes=60):
    if is_in_blacklist(user_id):
        return True

    key = f"user:{user_id}:actions"
    actions = redis_client.lrange(key, 0, -1)
    now = datetime.utcnow()
    time_window = now - timedelta(minutes=time_window_minutes)
    
    purchases = [json.loads(action.decode('utf-8')) for action in actions if 'purchase' in action.decode('utf-8') and product_id in action.decode('utf-8')]
    
    total_quantity = 0
    for action in purchases:
        try:
            timestamp_str = action['timestamp']
            action_type = action['action']
            prod_id = action['product_id']
            quantity = action['quantity']
            timestamp = datetime.strptime(timestamp_str, "%Y-%m-%d %H:%M:%S %Z")
            if timestamp >= time_window:
                total_quantity += int(quantity)
        except (KeyError, ValueError) as e:
            print(f"Skipping invalid action data: {action} - Error: {e}")
    
    if total_quantity > max_quantity:
        add_to_blacklist(user_id)
        return True
    return False

# Cảnh báo đơn hàng có giá trị lớn bất thường
def detect_large_value_order(user_id, price, threshold=1000):
    if price > threshold:
        add_to_blacklist(user_id)
        return True
    return False

# Cảnh báo thời gian mua bất thường của người dùng
def detect_unusual_purchase_time(user_id, timestamp, start_hour=0, end_hour=6):
    purchase_time = datetime.strptime(timestamp, "%Y-%m-%d %H:%M:%S %Z").time()
    if purchase_time >= datetime.strptime(str(start_hour), "%H").time() and purchase_time <= datetime.strptime(str(end_hour), "%H").time():
        add_to_blacklist(user_id)
        return True
    return False

# Ngăn chặn các hành vi brute-force
def detect_brute_force(user_id):
    key = f"user:{user_id}:actions"
    actions = redis_client.lrange(key, 0, -1)
    if len(actions) > 100:
        add_to_blacklist(user_id)
        return True
    return False

# Ngăn chặn các hành vi lạm dụng hủy đơn hàng
def detect_cancel_abuse(user_id, product_id):
    key = f"user:{user_id}:actions"
    actions = redis_client.lrange(key, 0, -1)
    cancels = [json.loads(action.decode('utf-8')) for action in actions if 'cancel' in action.decode('utf-8') and product_id in action.decode('utf-8')]
    if len(cancels) > 5:
        add_to_blacklist(user_id)
        return True
    return False