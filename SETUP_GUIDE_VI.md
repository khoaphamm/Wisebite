# 🚀 Hướng dẫn chạy ứng dụng Wisebite

## 📋 Yêu cầu hệ thống
- **Docker Desktop** (bắt buộc)
- **Git** (để clone repo)
- **Visual Studio Code** (khuyến nghị)

## 🛠️ Cách chạy ứng dụng (Đơn giản nhất)

### Bước 1: Clone repository
```bash
git clone https://github.com/khoaphamm/Wisebite.git
cd Wisebite
```

### Bước 2: Mở Docker Desktop
- Mở ứng dụng **Docker Desktop**
- Đợi Docker khởi động hoàn tất (biểu tượng Docker không còn nhấp nháy)

### Bước 3: Chạy Backend API

```bash
# Di chuyển vào thư mục backend
cd backend

# Chạy lệnh này để build và start tất cả services
docker-compose up --build -d

# Đợi khoảng 30 giây để database khởi động
# Sau đó chạy migration để tạo database structure
docker-compose exec app uv run alembic upgrade head

# Tạo dữ liệu mẫu ban đầu
docker-compose exec app uv run python -c "
from app.initial_db import populate_store_and_categories, create_initial_superuser
populate_store_and_categories()
create_initial_superuser()
"
```

### Bước 4: Kiểm tra Backend hoạt động
Mở trình duyệt và truy cập:
- **API Documentation**: http://localhost:8000/docs
- **Test API**: http://localhost:8000/api/v1/surprise-bag/

Nếu thấy trang web hiển thị đúng → Backend đã chạy thành công! ✅

### Bước 5: Chạy Customer App (Android)

```bash
# Di chuyển vào thư mục customer app
cd ../frontend/WisebiteCustomer

# Mở bằng Android Studio
# Hoặc chạy lệnh:
./gradlew assembleDebug
```

### Bước 6: Chạy Merchant App (Android)

```bash
# Di chuyển vào thư mục merchant app
cd ../WisebiteMerchant

# Mở bằng Android Studio
# Hoặc chạy lệnh:
./gradlew assembleDebug
```

## 🔧 Nếu gặp lỗi

### ❌ "could not translate host name 'db'"
```bash
# Chạy lệnh này thay vì chạy alembic trực tiếp:
docker-compose exec app uv run alembic upgrade head
```

### ❌ "relation already exists"
```bash
# Đánh dấu migration hiện tại đã được chạy
docker-compose exec app uv run alembic stamp add_categories_inventory

# Sau đó chạy migration tiếp
docker-compose exec app uv run alembic upgrade head
```

### ❌ "Docker is not running"
1. Mở Docker Desktop
2. Đợi Docker khởi động xong
3. Chạy lại lệnh `docker-compose up --build -d`

### ❌ "Port 8000 already in use"
```bash
# Tìm process đang dùng port 8000
netstat -ano | findstr :8000

# Kill process đó hoặc đổi port trong docker-compose.yml
```

### ❌ Reset toàn bộ (Clean start)
```bash
# Stop tất cả containers và xóa data
docker-compose down -v

# Rebuild và start lại
docker-compose up --build -d

# Chạy lại migration và tạo data mẫu
docker-compose exec app uv run alembic upgrade head
docker-compose exec app uv run python -c "
from app.initial_db import populate_store_and_categories, create_initial_superuser
populate_store_and_categories()
create_initial_superuser()
"
```

## 📱 Cách test ứng dụng

### Test Backend API
1. Mở http://localhost:8000/docs
2. Test endpoint `/api/v1/surprise-bag/` → Phải trả về `{"data":[],"count":0}`
3. Test endpoint `/api/v1/customer/stores/` → Phải có danh sách stores

### Test Customer App
1. Mở app trên emulator/device
2. Kiểm tra HomeScreen hiển thị danh sách stores
3. Test browse surprise bags
4. Test tạo order

### Test Merchant App  
1. Mở app trên emulator/device
2. Login với tài khoản merchant
3. Test tạo surprise bag mới
4. Test quản lý orders

## 🌟 Dữ liệu mẫu có sẵn

Sau khi chạy setup, system sẽ có:
- **Categories**: Combo, Thịt/Cá, Rau/Củ, Trái cây, Bánh mì
- **Sample stores** với địa chỉ Việt Nam
- **Sample surprise bags** với giá VND
- **Admin user** để test

## 📝 Scripts hữu ích

### Windows PowerShell
```powershell
# Chạy full setup tự động (nếu có file)
.\setup-db.ps1

# Chạy tests
.\run-tests.ps1
```

### Kiểm tra logs
```bash
# Xem logs của backend
docker-compose logs -f app

# Xem logs của database  
docker-compose logs -f db

# Xem tất cả logs
docker-compose logs -f
```

### Stop/Start services
```bash
# Stop tất cả
docker-compose down

# Start lại
docker-compose up -d

# Restart một service cụ thể
docker-compose restart app
```

## 💡 Tips cho Developer

### Development workflow
1. **Backend changes**: Restart `docker-compose restart app`
2. **Database changes**: Run `docker-compose exec app uv run alembic upgrade head`
3. **Clean rebuild**: `docker-compose down -v && docker-compose up --build -d`

### Useful endpoints
- **API Docs**: http://localhost:8000/docs
- **API Alternative Docs**: http://localhost:8000/redoc
- **Health Check**: http://localhost:8000/api/v1/surprise-bag/

### Database access
```bash
# Connect to PostgreSQL database
docker-compose exec db psql -U postgres -d wisebite_db

# View tables
\dt

# View data
SELECT * FROM stores;
SELECT * FROM surprise_bags;
```

## 🚨 Troubleshooting nhanh

| Lỗi | Giải pháp |
|-----|-----------|
| Docker not running | Mở Docker Desktop, đợi khởi động |
| Port 8000 in use | Kill process hoặc đổi port |
| Migration fails | Chạy `docker-compose exec app uv run alembic upgrade head` |
| No data returned | Chạy lại script tạo data mẫu |
| App crashes | Check logs: `docker-compose logs -f app` |

## 📞 Liên hệ hỗ trợ

Nếu vẫn gặp vấn đề:
1. Check logs: `docker-compose logs -f`
2. Reset toàn bộ: `docker-compose down -v && docker-compose up --build -d`
3. Hỏi trên group chat hoặc tạo issue trên GitHub

---
**Chúc bạn code vui vẻ! 🎉**