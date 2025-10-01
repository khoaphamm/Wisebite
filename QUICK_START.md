# 🚀 QUICK START - Chạy app trong 5 phút

## Bước 1: Mở Docker Desktop 
- Bật Docker Desktop trên máy
- Đợi nó khởi động xong (icon không nhấp nháy nữa)

## Bước 2: Clone và setup
```bash
git clone https://github.com/khoaphamm/Wisebite.git
cd Wisebite/backend
```

## Bước 3: Chạy script tự động
**Windows Command Prompt:**
```bash
setup-easy.bat
```

**Windows PowerShell:**
```bash
./setup-easy.ps1
```

Script sẽ tự động:
- ✅ Build containers  
- ✅ Start database
- ✅ Run migrations
- ✅ Tạo data mẫu
- ✅ Test API

## Bước 4: Kiểm tra
Mở trình duyệt: http://localhost:8000/docs

Thấy trang Swagger docs → **THÀNH CÔNG!** 🎉

## Bước 5: Chạy mobile apps
```bash
# Customer app
cd ../frontend/WisebiteCustomer
# Mở bằng Android Studio và run

# Merchant app  
cd ../WisebiteMerchant
# Mở bằng Android Studio và run
```

## ❌ Nếu có lỗi
1. **Docker not running**: Bật Docker Desktop
2. **Port 8000 in use**: Restart máy hoặc kill process
3. **Migration fails**: Chạy lại script
4. **Reset everything**: 
   ```bash
   docker-compose down -v
   docker-compose up --build -d
   ```

## 📞 Cần hỗ trợ?
- Đọc file `SETUP_GUIDE_VI.md` (hướng dẫn chi tiết)
- Check logs: `docker-compose logs -f app`
- Hỏi trên chat nhóm

**That's it! 🚀**