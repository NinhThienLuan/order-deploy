# Order Service - Standalone Deployment

Service này đã được tách ra khỏi multi-module và có thể deploy độc lập.

## ✅ Đã cấu hình

- ✅ Standalone pom.xml (không phụ thuộc parent multi-module)
- ✅ Standalone Dockerfile (build chỉ service này)
- ✅ Tích hợp Eureka Client (kết nối tới Eureka server đã deploy)
- ✅ Tích hợp API Gateway (request sẽ đi qua Gateway)
- ✅ MySQL database
- ✅ MoMo & VNPay payment integration
- ✅ Health check endpoints

## 🚀 Cách Deploy

### Option 0: Render / Railway (khong Dockerfile)

Buildpack se tu detect Maven project.

**Root Directory**
- `order-service/product-order-payment`

**Build Command**
```bash
./mvnw clean package -DskipTests
```

**Start Command**
```bash
java -Dspring.profiles.active=cloud -jar target/order-service-1.0.0-SNAPSHOT.jar
```

**Required Environment Variables**
- `PORT` (Render/Railway set san)
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

**Optional (neu co gateway/discovery)**
- `EUREKA_ENABLED=true`
- `EUREKA_SERVER_URL=...`
- `FRANCHISE_STORE_BASE_URL=...`
- `INVENTORY_BASE_URL=...`

Cloud profile file: `src/main/resources/application-cloud.properties`

### Option 1: Docker Compose (Khuyến nghị)

```bash
# Build và run (bao gồm MySQL)
docker-compose -f docker-compose.standalone.yml up -d --build

# Xem logs
docker-compose -f docker-compose.standalone.yml logs -f order-service

# Stop
docker-compose -f docker-compose.standalone.yml down
```

### Option 2: Docker Build riêng

```bash
# Build image
docker build -t order-service:latest .

# Run container
docker run -d \
  --name order-service \
  -p 8081:8081 \
  -e EUREKA_SERVER_URL=http://3.27.221.137:8761/eureka/ \
  -e DB_URL=jdbc:mysql://mysql:3306/franchise?useSSL=false \
  -e DB_USERNAME=franchise \
  -e DB_PASSWORD=franchise_secret \
  order-service:latest
```

### Option 3: Maven local (Development)

```bash
# Build
mvn clean package -DskipTests

# Run với production profile
java -jar -Dspring.profiles.active=prod target/order-service-1.0.0-SNAPSHOT.jar
```

## 🔧 Cấu hình quan trọng

### Kết nối Eureka
Service sẽ tự đăng ký với Eureka server tại:
```
EUREKA_SERVER_URL=http://3.27.221.137:8761/eureka/
```

### Database
- MySQL 8.0
- Database: `franchise`
- Auto-create tables với JPA (ddl-auto=update)

### Ports
- **8081**: Order Service API
- **3306**: MySQL (nếu dùng docker-compose)

## 📝 Environment Variables

Tất cả config có thể override qua environment variables:

| Variable | Default | Mô tả |
|----------|---------|-------|
| `PORT` | 8081 | Port của service |
| `EUREKA_SERVER_URL` | http://eureka-server:8761/eureka/ | Eureka server URL |
| `DB_URL` | jdbc:mysql://mysql:3306/franchise | Database URL |
| `DB_USERNAME` | franchise | Database user |
| `DB_PASSWORD` | franchise_secret | Database password |
| `JWT_SECRET` | (xem file) | JWT secret key |
| `MOMO_*` | (xem file) | MoMo payment config |
| `VNPAY_*` | (xem file) | VNPay payment config |

## 🔍 Health Check

```bash
# Health endpoint
curl http://localhost:8081/actuator/health

# Swagger UI
http://localhost:8081/swagger-ui.html

# API Docs
http://localhost:8081/v3/api-docs
```

## ⚠️ CORS Configuration

**QUAN TRỌNG**: Service này **KHÔNG cấu hình CORS** vì:
- CORS đã được cấu hình ở API Gateway
- Nếu cấu hình ở cả 2 nơi sẽ bị duplicate headers

Nếu cần test trực tiếp (bypass Gateway), uncomment CORS config trong:
- `WebSecurityConfig.java` hoặc
- Thêm `@CrossOrigin` vào controllers

## 🏗️ Kiến trúc

```
Frontend (port 3000)
    ↓
API Gateway (port 8080) ← CORS configured here
    ↓
Eureka Server (port 8761)
    ↓
Order Service (port 8081) ← This service
    ↓
MySQL (port 3306)
```

## 📦 Build Info

- **Java**: 21
- **Spring Boot**: 3.3.5
- **Spring Cloud**: 2023.0.3
- **Maven**: 3.9+

## 🐛 Troubleshooting

### Service không đăng ký với Eureka
```bash
# Kiểm tra Eureka server có chạy không
curl http://3.27.221.137:8761

# Kiểm tra logs
docker logs order-service
```

### Database connection error
```bash
# Kiểm tra MySQL container
docker ps | grep mysql

# Test connection
docker exec -it franchise-mysql mysql -ufranchise -pfranchise_secret franchise
```

### Port conflict
```bash
# Kiểm tra port đang dùng
netstat -ano | findstr :8081

# Hoặc đổi port
docker run -p 8082:8081 ...
```
