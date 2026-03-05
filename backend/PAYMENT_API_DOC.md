# Payment & Order API Documentation

> **Base URL:** `http://localhost:8080`
> **Auth:** JWT HttpOnly Cookie (gửi kèm mỗi request, trừ các endpoint public)
> **Content-Type:** `application/json`

---

## Mục lục

| # | Nhóm | API | Assignee      |
|---|------|-----|---------------|
| 1 | Payment Gateway | `GET /v1/payments/methods` | Triết         |
| 2 | Payment Gateway | `POST /v1/orders/{id}/payments` | Triết         |
| 3 | Payment Gateway | `POST /v1/payments/momo/callback` | Khánh + Triết |
| 4 | Payment Gateway | `GET /v1/payments/momo/return` | Khánh + Triết |
| 5 | View & Monitor | `GET /v1/payments/{orderId}/status` | G.Huy         |
| 6 | View & Monitor | `GET /v1/payments` (user payment history) | G.Huy         |
| 7 | View & Monitor | `GET /v1/admin/transactions` | G.Huy         |
| 8 | Order | `POST /v1/orders` | —             |
| 9 | Order | `GET /v1/orders/{id}` | —             |
| 10 | Order | `GET /v1/orders/{id}/status` | —             |
| 11 | Order | `GET /v1/orders` (order history) | —             |
| 12 | Order | `PATCH /v1/orders/{id}/cancel` | —             |
| 13 | Refund | `POST /payments/{transaction_id}/refund-initiate` | Luân          |
| 14 | Refund | `GET /payments/refund-status/{refund_id}` | Luân          |

---

## Cấu trúc Response chung

Tất cả API trả về format `ApiResponse<T>`:

```json
{
  "code": 200,
  "message": "Success",
  "result": { ... },
  "errors": null,
  "errorKey": null,
  "timestamp": "2026-03-03T10:00:00Z",
  "path": "/v1/orders"
}
```

Khi lỗi:

```json
{
  "code": 30005,
  "message": "MoMo error (11): Quyền truy cập bị từ chối",
  "result": null,
  "errorKey": "payment.provider_error",
  "timestamp": "2026-03-03T10:00:00Z",
  "path": "/v1/orders/{id}/payments"
}
```

---

## Enums tham chiếu

| Enum | Values |
|------|--------|
| `PaymentMethod` | `MOMO`, `CASH`, `VNPAY` *(disabled)*, `PAYOS` *(disabled)*, `WALLET` *(disabled)* |
| `PaymentStatus` | `PENDING`, `PAID`, `FAILED`, `REFUNDED` |
| `OrderStatus` | `PENDING`, `PAID`, `PREPARING`, `READY`, `COMPLETED`, `CANCELED` |
| `TransactionStatus` | `PENDING`, `SUCCESS`, `FAILED` |
| `TransactionType` | `PAYMENT`, `REFUND`, `TOPUP` |
| MoMo `requestType` | Defined in `MomoRequestType` enum: `captureWallet` *(default)*, `payWithATM`, `payWithCC` — enabled/disabled via `application-dev.properties` |

---

## Nhóm 1 — Payment Gateway (Xử lý giao dịch)

### 1.1 `GET /v1/payments/methods`

> **Lấy danh sách phương thức thanh toán khả dụng** — *Assignee: Triết*
>
> Đọc từ `PaymentMethodProperties` (`application-*.properties`), không hardcode.

| Thuộc tính | Giá trị |
|---|---|
| Auth | ❌ Public (không cần đăng nhập) |
| Method | `GET` |
| URL | `/v1/payments/methods` |

#### Response `200 OK`

```json
{
  "code": 200,
  "message": "Payment methods loaded successfully",
  "result": [
    {
      "code": "MOMO",
      "name": "MoMo",
      "description": "Thanh toán qua MoMo",
      "icon": "momo",
      "subOptions": [
        {
          "code": "captureWallet",
          "name": "QR / Ví MoMo",
          "description": "Quét mã QR hoặc mở app MoMo"
        },
        {
          "code": "payWithATM",
          "name": "Thẻ ATM nội địa",
          "description": "Vietcombank, BIDV, MB, Techcombank... (cần merchant riêng)",
          "enabled": false
        },
        {
          "code": "payWithCC",
          "name": "Thẻ quốc tế",
          "description": "Visa, Mastercard, JCB (cần merchant riêng)",
          "enabled": false
        }
      ]
    },
    {
      "code": "VNPAY",
      "name": "VNPay",
      "description": "Thanh toán qua VNPay QR",
      "icon": "vnpay",
      "enabled": false
    },
    {
      "code": "PAYOS",
      "name": "PayOS",
      "description": "Thanh toán qua PayOS (chuyển khoản ngân hàng)",
      "icon": "payos",
      "enabled": false
    },
    {
      "code": "CASH",
      "name": "Tiền mặt",
      "description": "Thanh toán khi nhận hàng (COD)",
      "icon": "cash"
    },
    {
      "code": "WALLET",
      "name": "Ví nội bộ",
      "description": "Thanh toán bằng số dư ví",
      "icon": "wallet",
      "enabled": false
    }
  ]
}
```

> **Lưu ý FE:** Nếu field `enabled` không có hoặc `true` → hiển thị. Nếu `enabled: false` → ẩn hoặc disable.  
> Với MoMo, kiểm tra `subOptions[].enabled` để biết kênh con nào đang hoạt động.
>
> **Architecture:** Controller → `PaymentMethodService` → `PaymentMethodProperties` → `application-dev.properties`. Muốn thêm/sửa/tắt method chỉ cần sửa file properties, không cần rebuild code.

---

### 1.2 `POST /v1/orders/{id}/payments`

> **Tạo thanh toán cho đơn hàng** — trả về Payment URL (MoMo) hoặc xác nhận PAID (CASH) — *Assignee: Khánh*

| Thuộc tính | Giá trị |
|---|---|
| Auth | ✅ JWT Cookie (CUSTOMER) |
| Method | `POST` |
| URL | `/v1/orders/{id}/payments` |
| Path Param | `id` — UUID của đơn hàng |

#### Request Body

```json
{
  "paymentMethod": "MOMO",
  "amount": 110000,
  "momoRequestType": "captureWallet"
}
```

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `paymentMethod` | `PaymentMethod` | ✅ | `MOMO`, `CASH` (hiện tại chỉ 2 phương thức active) |
| `amount` | `BigDecimal` | ✅ | Số tiền, phải > 0, **phải khớp** tổng đơn hàng |
| `momoRequestType` | `String` | ❌ | Chỉ dùng khi `paymentMethod = MOMO`. Mặc định `"captureWallet"`. Giá trị hợp lệ định nghĩa trong enum `MomoRequestType` và phải enabled trong config |

#### Xử lý logic bên trong (Business Rules)

```
1. Kiểm tra order tồn tại → 404 nếu không
2. Kiểm tra order thuộc user hiện tại → 403 nếu không
3. Kiểm tra order.status == PENDING → 409 nếu đã PAID, 400 nếu status khác
4. Tính tổng tiền từ orderItems, so sánh với amount → 400 nếu không khớp
5. Kiểm tra idempotency: tìm PaymentEntity(orderId, PENDING)
   - Nếu đã có → tái sử dụng (không tạo mới)
   - Nếu chưa → tạo PaymentEntity mới (status = PENDING)
6. Nếu MOMO → validate momoRequestType từ PaymentMethodService (config-driven)
   - Nếu request type disabled → 400 INVALID_PAYMENT_METHOD
   - Nếu enabled → gọi MoMo API /create → trả payUrl
7. Nếu CASH → set payment = PAID, order = PAID → trả kết quả
```

#### Response `200 OK` — MoMo

```json
{
  "code": 200,
  "message": "Success",
  "result": {
    "paymentId": "a1b2c3d4-...",
    "orderId": "e5f6g7h8-...",
    "paymentMethod": "MOMO",
    "amountPaid": 110000,
    "paymentStatus": "PENDING",
    "orderStatus": "PENDING",
    "paymentUrl": "https://test-payment.momo.vn/gw_payment/transactionProcessor?..."
  }
}
```

> **FE:** Redirect user tới `paymentUrl` để thanh toán. Sau khi xong, MoMo redirect về `returnUrl` và gọi `callback` (IPN).

#### Response `200 OK` — CASH

```json
{
  "code": 200,
  "message": "Success",
  "result": {
    "paymentId": "a1b2c3d4-...",
    "orderId": "e5f6g7h8-...",
    "paymentMethod": "CASH",
    "amountPaid": 110000,
    "paymentStatus": "PAID",
    "orderStatus": "PAID"
  }ok
}
```

> **Lưu ý:** Field `paymentUrl` chỉ xuất hiện khi `paymentMethod = MOMO` (được ẩn bởi `@JsonInclude(NON_NULL)`).

#### Error Responses

| HTTP | Code | Khi nào |
|------|------|---------|
| 404 | `404` | Order không tồn tại |
| 403/410 | `403` | Order không thuộc user |
| 409 | `20003` | Order đã thanh toán (`ORDER_ALREADY_PAID`) |
| 400 | `409` | Order status không hợp lệ (`INVALID_ORDER_STATUS`) |
| 400 | `30006` | Amount không khớp tổng đơn (`PAYMENT_AMOUNT_MISMATCH`) |
| 400 | `30004` | MoMo request type disabled trong config (`INVALID_PAYMENT_METHOD`) |
| 502 | `30005` | Lỗi MoMo (ví dụ: kết nối thất bại) (`PAYMENT_PROVIDER_ERROR`) |

---

### 1.3 `POST /v1/payments/momo/callback`

> **MoMo IPN Webhook** — MoMo server-to-server gọi ngược để thông báo kết quả thanh toán — *Assignee: Khánh + Triết*

| Thuộc tính | Giá trị |
|---|---|
| Auth | ❌ Public (MoMo gọi, không có JWT) |
| Method | `POST` |
| URL | `/v1/payments/momo/callback` |
| Gọi bởi | **MoMo server** (IPN — Instant Payment Notification) |

#### Request Body (do MoMo gửi)

```json
{
  "partnerCode": "MOMOLRJZ20181206",
  "orderId": "MOMO-e5f6g7h8-...-1709456789000",
  "requestId": "uuid-...",
  "amount": "110000",
  "orderInfo": "Thanh toan don hang #e5f6g7h8-...",
  "orderType": "momo_wallet",
  "transId": "3004789051",
  "resultCode": "0",
  "message": "Thành công.",
  "payType": "qr",
  "responseTime": "1709456800000",
  "extraData": "eyJvcmRlcklkIjoiZTVmNmc3aDgtLi4uIn0=",
  "signature": "abc123..."
}
```

#### Xử lý logic bên trong

```
1. Verify HMAC-SHA256 signature → reject nếu sai
2. Decode extraData (Base64) → lấy orderId gốc (UUID)
3. Tìm PaymentEntity(orderId, PENDING)
   - Không tìm thấy → return OK (idempotent, có thể đã xử lý)
4. Tạo TransactionEntity:
   - vnpTxnRef = MoMo orderId
   - vnpTransactionNo = transId
   - vnpResponseCode = resultCode
   - vnpBankCode = payType (e.g. "qr", "napas")
   - type = PAYMENT
   - amount = amount
   - status = SUCCESS / FAILED
5. Nếu resultCode == 0 (thành công):
   - payment.status = PAID
   - payment.transactionId = transId
   - order.status = PAID
6. Nếu resultCode != 0 (thất bại):
   - payment.status = FAILED
   - payment.errorMessage = "MoMo resultCode=X: message"
```

#### Response (trả về cho MoMo)

Thành công:
```json
{ "resultCode": 0, "message": "OK" }
```

Signature sai:
```json
{ "resultCode": 1, "message": "Invalid signature" }
```

ExtraData parse lỗi:
```json
{ "resultCode": 1, "message": "Invalid extraData" }
```

> ⚠️ **Quan trọng:** Endpoint này phải public, KHÔNG có JWT filter. MoMo timeout 15s — response phải nhanh.  
> ⚠️ **Máy local:** Cần dùng **ngrok** để expose callback URL ra internet.

---

### 1.4 `GET /v1/payments/momo/return`

> **MoMo Return URL** — User bị redirect về đây sau khi thanh toán xong trên MoMo — *Assignee: Khánh + Triết*

| Thuộc tính | Giá trị |
|---|---|
| Auth | ❌ Public |
| Method | `GET` |
| URL | `/v1/payments/momo/return?partnerCode=...&orderId=...&resultCode=...&...` |
| Gọi bởi | **Trình duyệt** (MoMo redirect user về) |

#### Query Params (do MoMo append)

Các params tương tự callback: `partnerCode`, `orderId`, `requestId`, `amount`, `orderInfo`, `orderType`, `transId`, `resultCode`, `message`, `payType`, `responseTime`, `extraData`, `signature`.

#### Response `200 OK`

```json
{
  "success": true,
  "resultCode": 0,
  "message": "Thành công.",
  "momoOrderId": "MOMO-e5f6g7h8-...-1709456789000",
  "transId": "3004789051",
  "amount": "110000"
}
```

```json
{
  "success": false,
  "resultCode": 1006,
  "message": "Giao dịch bị từ chối bởi người dùng.",
  "momoOrderId": "MOMO-e5f6g7h8-...-1709456789000",
  "transId": null,
  "amount": "110000"
}
```

> **FE:** Dùng response này để hiển thị trang "Cảm ơn" hoặc "Thanh toán thất bại". Hoặc FE có thể chỉ dựa vào query param `resultCode` để redirect sang trang phù hợp, rồi gọi `GET /v1/payments/{orderId}/status` để xác nhận chính xác.

---

## Nhóm 2 — View & Monitor (Hiển thị & Kiểm tra)

### 2.1 `GET /v1/payments/{orderId}/status`

> **Kiểm tra trạng thái thanh toán** — hiển thị trên trang "Cảm ơn" hoặc polling — *Assignee: G.Huy*

| Thuộc tính | Giá trị |
|---|---|
| Auth | ✅ JWT Cookie (CUSTOMER, FRANCHISE_ADMIN, MANAGER) |
| Method | `GET` |
| URL | `/v1/payments/{orderId}/status` |
| Path Param | `orderId` — UUID của đơn hàng |

#### Xử lý logic

```
1. Tìm tất cả PaymentEntity theo orderId, sắp xếp paymentDate DESC
2. Lấy payment mới nhất
3. Kiểm tra quyền:
   - FRANCHISE_ADMIN / MANAGER → xem được tất cả
   - CUSTOMER → chỉ xem order của mình
4. Tìm TransactionEntity mới nhất của payment
5. Trả về PaymentStatusResponse
```

#### Response `200 OK`

```json
{
  "code": 200,
  "message": "Get payment status successfully",
  "result": {
    "orderId": "e5f6g7h8-...",
    "paymentId": "a1b2c3d4-...",
    "paymentMethod": "MOMO",
    "status": "PAID",
    "amountPaid": 110000,
    "transaction": {
      "vnpTxnRef": "MOMO-e5f6g7h8-...-1709456789000",
      "vnpTransactionNo": "3004789051",
      "vnpResponseCode": "0",
      "vnpBankCode": "qr"
    }
  }
}
```

> **Ghi chú field mapping cho MoMo:**  
> - `vnpTxnRef` → MoMo `orderId` (mã đơn MoMo, format: `MOMO-{uuid}-{timestamp}`)  
> - `vnpTransactionNo` → MoMo `transId` (mã giao dịch MoMo)  
> - `vnpResponseCode` → MoMo `resultCode` (`"0"` = thành công)  
> - `vnpBankCode` → MoMo `payType` (`"qr"`, `"napas"`, etc.)

#### Error Responses

| HTTP | Code | Khi nào |
|------|------|---------|
| 404 | `404` | Không tìm thấy payment cho orderId |
| 403 | `403` | Customer truy cập order không phải của mình |
| 400 | `400` | orderId không phải UUID hợp lệ |

---

### 2.2 `GET /v1/payments`

> **Lịch sử thanh toán của user** (Customer xem payment history, Admin xem tất cả) — *Assignee: G.Huy*

| Thuộc tính | Giá trị |
|---|---|
| Auth | ✅ JWT Cookie (CUSTOMER, FRANCHISE_ADMIN) |
| Method | `GET` |
| URL | `/v1/payments` |

#### Query Parameters

| Param | Type | Required | Mô tả |
|-------|------|----------|-------|
| `page` | `int` | ❌ | Trang (default: `0`, 0-indexed) |
| `size` | `int` | ❌ | Số bản ghi/trang (default: `10`, max: `50`) |
| `orderId` | `String` | ❌ | Lọc theo order ID |
| `status` | `String` | ❌ | Lọc theo payment status: `PENDING`, `PAID`, `FAILED`, `REFUNDED` |
| `fromDate` | `ISO 8601` | ❌ | Lọc từ ngày (format: `2026-03-01T00:00:00Z`) |
| `toDate` | `ISO 8601` | ❌ | Lọc đến ngày |
| `customerId` | `int` | ❌ | *(Admin only)* Lọc theo customer ID |
| `email` | `String` | ❌ | *(Admin only)* Lọc theo email customer |

#### Xử lý logic

```
- CUSTOMER → chỉ thấy payment của mình (filter theo userId từ JWT)
- FRANCHISE_ADMIN → thấy tất cả, có thể filter thêm
- Nếu fromDate có mà toDate thiếu → toDate = now
- Nếu toDate có mà fromDate thiếu → fromDate = 30 ngày trước
```

#### Response `200 OK`

```json
{
  "code": 1000,
  "message": "Payment log loaded successfully.",
  "result": {
    "data": [
      {
        "transactionId": "uuid-or-null",
        "orderId": "e5f6g7h8-...",
        "orderNumber": "ORD-001",
        "customerName": "Nguyễn Văn A",
        "paymentMethod": "MOMO",
        "amountPaid": 110000,
        "paymentDate": "2026-03-03T10:30:00Z",
        "status": "PAID",
        "errorMessage": null
      }
    ],
    "pagination": {
      "currentPage": 0,
      "totalPages": 5,
      "totalElements": 48,
      "pageSize": 10
    }
  }
}
```

> **Lưu ý:** Field `errorMessage` chỉ hiện cho Admin. Customer luôn nhận `null`.

---

### 2.3 `GET /v1/admin/transactions`

> **Danh sách giao dịch cho quản trị** — dùng để đối soát dòng tiền — *Assignee: G.Huy*

| Thuộc tính | Giá trị |
|---|---|
| Auth | ✅ JWT Cookie (FRANCHISE_ADMIN, MANAGER) |
| Method | `GET` |
| URL | `/v1/admin/transactions` |

#### Query Parameters

| Param | Type | Required | Mô tả |
|-------|------|----------|-------|
| `page` | `int` | ❌ | Trang (default: `1`, **1-indexed**) |
| `size` | `int` | ❌ | Số bản ghi/trang (default: `10`, max: `50`) |
| `status` | `String` | ❌ | Lọc theo payment status |
| `paymentMethod` | `String` | ❌ | Lọc theo phương thức: `MOMO`, `CASH`, etc. |
| `responseCode` | `String` | ❌ | *(Chưa dùng trong query)* |
| `fromDate` | `ISO Date` | ❌ | Lọc từ ngày (`2026-03-01`) |
| `toDate` | `ISO Date` | ❌ | Lọc đến ngày (`2026-03-03`) |

#### Response `200 OK`

```json
{
  "code": 200,
  "message": "Get transaction list successfully",
  "result": {
    "content": [
      {
        "transactionId": "txn-uuid-...",
        "orderId": "order-uuid-...",
        "customerId": "user-uuid-...",
        "paymentId": "payment-uuid-...",
        "paymentMethod": "MOMO",
        "paymentStatus": "PAID",
        "amountPaid": 110000,
        "vnpTxnRef": "MOMO-order-uuid-...-1709456789000",
        "vnpTransactionNo": "3004789051",
        "vnpResponseCode": "0",
        "vnpBankCode": "qr",
        "createdDate": "2026-03-03T10:30:00Z"
      }
    ],
    "page": 1,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3
  }
}
```

#### Error Responses

| HTTP | Code | Khi nào |
|------|------|---------|
| 403 | — | User không có role FRANCHISE_ADMIN / MANAGER |
| 400 | `2000` | Pagination params không hợp lệ (page < 1, size < 1, size > 50) |
| 400 | `2002` | fromDate > toDate |

---

## Nhóm 3 — Order (Quản lý đơn hàng)

### 3.1 `POST /v1/orders`

> **Tạo đơn hàng mới**

| Thuộc tính | Giá trị |
|---|---|
| Auth | ✅ JWT Cookie |
| Method | `POST` |
| URL | `/v1/orders` |

#### Request Body

```json
{
  "items": [
    {
      "productId": "00000000-0000-0000-0005-000000000002",
      "quantity": 2
    },
    {
      "productId": "00000000-0000-0000-0005-000000000003",
      "quantity": 1
    }
  ]
}
```

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `items` | `List<OrderItemRequest>` | ✅ | Danh sách sản phẩm, không được rỗng |
| `items[].productId` | `UUID` | ✅ | ID sản phẩm |
| `items[].quantity` | `int` | ✅ | Số lượng, ≥ 1 |

#### Xử lý logic

```
1. Tìm customer theo JWT → 404 nếu không tồn tại
2. Tạo OrderEntity (status = PENDING)
3. Với mỗi item:
   - Tìm ProductEntity → 404 nếu không tồn tại
   - Kiểm tra product.active → 400 nếu inactive (PRODUCT_OUT_OF_STOCK)
   - Tìm ProductVariantEntity active đầu tiên → lấy giá
   - Tạo OrderItemEntity
4. Tính tổng = Σ(unitPrice × quantity)
5. Lưu order + orderItems
```

#### Response `201 Created`

```json
{
  "code": 201,
  "message": "Resource created successfully",
  "result": {
    "orderId": "e5f6g7h8-...",
    "status": "PENDING",
    "orderTime": "2026-03-03T17:30:00",
    "totalAmount": 165000,
    "items": [
      {
        "productId": "00000000-0000-0000-0005-000000000002",
        "productName": "Latte",
        "quantity": 2,
        "unitPrice": 55000,
        "subtotal": 110000
      },
      {
        "productId": "00000000-0000-0000-0005-000000000003",
        "productName": "Cappuccino",
        "quantity": 1,
        "unitPrice": 55000,
        "subtotal": 55000
      }
    ]
  }
}
```

---

### 3.2 `GET /v1/orders/{id}`

> **Chi tiết đơn hàng**

| Thuộc tính | Giá trị |
|---|---|
| Auth | ✅ JWT Cookie (CUSTOMER, STORE_MANAGER, ADMIN) |
| Method | `GET` |
| URL | `/v1/orders/{id}` |
| Path Param | `id` — UUID string |

#### Response `200 OK`

```json
{
  "code": 1000,
  "message": "Get order detail successfully",
  "result": {
    "orderId": "e5f6g7h8-...",
    "orderNumber": "ORD-001",
    "status": "PAID",
    "orderType": "DELIVERY",
    "orderTime": "2026-03-03T17:30:00",
    "customer": {
      "customerId": "user-uuid-...",
      "customerName": "Alice Nguyen",
      "contactNumber": "0901234567",
      "deliveryAddress": "123 Nguyễn Huệ, Q1, HCM"
    },
    "items": [
      {
        "productId": "00000000-0000-0000-0005-000000000002",
        "productName": "Latte",
        "quantity": 2,
        "unitPrice": 55000,
        "subtotal": 110000
      }
    ],
    "pricing": {
      "subtotal": 110000,
      "discount": 0,
      "totalAmount": 110000
    },
    "payment": {
      "paymentMethod": "MOMO",
      "amountPaid": 110000,
      "paymentStatus": "Paid",
      "paymentDate": "2026-03-03T10:35:00Z"
    },
    "createdAt": "2026-03-03T17:30:00",
    "updatedAt": "2026-03-03T17:35:00"
  }
}
```

---

### 3.3 `GET /v1/orders/{id}/status`

> **Trạng thái đơn hàng**

| Thuộc tính | Giá trị |
|---|---|
| Auth | ✅ JWT Cookie |
| Method | `GET` |
| URL | `/v1/orders/{id}/status` |

#### Response `200 OK`

```json
{
  "result": {
    "id": "e5f6g7h8-...",
    "status": "PAID",
    "lastUpdated": "2026-03-03T17:35:00"
  },
  "isSuccess": true,
  "statusCode": 200,
  "message": "Get order status successfully"
}
```

---

### 3.4 `GET /v1/orders`

> **Lịch sử đơn hàng** (Admin/Manager only)

| Thuộc tính | Giá trị |
|---|---|
| Auth | ✅ JWT Cookie (FRANCHISE_ADMIN, MANAGER) |
| Method | `GET` |
| URL | `/v1/orders` |

#### Query Parameters

| Param | Type | Required | Mô tả |
|-------|------|----------|-------|
| `page` | `int` | ❌ | Trang (default: `1`, **1-indexed**) |
| `size` | `int` | ❌ | Số bản ghi/trang (default: `10`, max: `100`) |
| `status` | `String` | ❌ | Lọc theo OrderStatus |
| `branchId` | `Long` | ❌ | Lọc theo chi nhánh |
| `fromDate` | `ISO Date` | ❌ | Từ ngày (`2026-03-01`) |
| `toDate` | `ISO Date` | ❌ | Đến ngày |

#### Response `200 OK`

```json
{
  "result": {
    "content": [
      {
        "id": "e5f6g7h8-...",
        "totalAmount": 110000,
        "status": "PAID",
        "createdAt": "2026-03-03T17:30:00"
      }
    ],
    "page": 1,
    "size": 10,
    "totalElements": 15,
    "totalPages": 2
  },
  "isSuccess": true,
  "statusCode": 200,
  "message": "Get order list successfully"
}
```

---

### 3.5 `PATCH /v1/orders/{id}/cancel`

> **Hủy đơn hàng**

| Thuộc tính | Giá trị |
|---|---|
| Auth | ✅ JWT Cookie |
| Method | `PATCH` |
| URL | `/v1/orders/{id}/cancel` |

#### Xử lý logic

```
- Chỉ hủy được khi status = PENDING hoặc PAID
- Không hủy được khi: PREPARING, READY, COMPLETED, CANCELED
- Chỉ owner (customer) mới hủy được
```

#### Response `200 OK`

```json
{
  "code": 200,
  "message": "Success",
  "result": {
    "orderId": "e5f6g7h8-...",
    "canceledAt": "2026-03-03T17:40:00"
  }
}
```

#### Error Responses

| HTTP | Code | Khi nào |
|------|------|---------|
| 404 | `404` | Order không tồn tại |
| 410 | `403` | Không phải owner |
| 400 | `409` | Status không cho phép hủy |

---

## Nhóm 4 — Refund (Hoàn tiền)

> ⚠️ **Status: CHƯA IMPLEMENT** — Cần Luân triển khai

### 4.1 `POST /v1/payments/{transactionId}/refund-initiate`

> **Gửi yêu cầu hoàn tiền** — *Assignee: Luân*

| Thuộc tính | Giá trị |
|---|---|
| Auth | ✅ JWT Cookie (CUSTOMER hoặc ADMIN) |
| Method | `POST` |
| URL | `/v1/payments/{transactionId}/refund-initiate` |
| Path Param | `transactionId` — UUID của transaction |

#### Request Body (đề xuất)

```json
{
  "reason": "Khách hàng hủy đơn",
  "amount": 110000
}
```

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `reason` | `String` | ✅ | Lý do hoàn tiền |
| `amount` | `BigDecimal` | ❌ | Số tiền hoàn (mặc định = toàn bộ). Hỗ trợ hoàn một phần. |

#### Expected Business Rules

```
1. Tìm TransactionEntity → 404 nếu không tồn tại
2. Kiểm tra transaction.status == SUCCESS → 400 nếu không
3. Kiểm tra order chưa giao (status != COMPLETED) → 400 nếu đã giao
4. Tìm PaymentEntity tương ứng
5. Gọi MoMo Refund API (nếu paymentMethod = MOMO)
6. Tạo TransactionEntity mới (type = REFUND, status = PENDING)
7. Cập nhật PaymentEntity.status = REFUNDED
```

#### Response (đề xuất) `200 OK`

```json
{
  "code": 200,
  "message": "Refund initiated successfully",
  "result": {
    "refundId": "refund-uuid-...",
    "transactionId": "txn-uuid-...",
    "orderId": "order-uuid-...",
    "refundAmount": 110000,
    "status": "PENDING",
    "reason": "Khách hàng hủy đơn",
    "createdAt": "2026-03-03T17:45:00Z"
  }
}
```

#### MoMo Refund API Reference

```
POST https://test-payment.momo.vn/v2/gateway/api/refund

Body:
{
  "partnerCode": "MOMOLRJZ20181206",
  "orderId": "REFUND-{uuid}-{timestamp}",
  "requestId": "{uuid}",
  "amount": 110000,
  "transId": 3004789051,           // MoMo transId gốc
  "lang": "vi",
  "description": "Hoàn tiền đơn hàng #...",
  "signature": "hmac-sha256(...)"
}

Signature raw:
accessKey={accessKey}&amount={amount}&description={desc}&orderId={orderId}
&partnerCode={partnerCode}&requestId={requestId}&transId={transId}
```

---

### 4.2 `GET /v1/payments/refund-status/{refundId}`

> **Theo dõi trạng thái hoàn tiền** — *Assignee: Luân*

| Thuộc tính | Giá trị |
|---|---|
| Auth | ✅ JWT Cookie |
| Method | `GET` |
| URL | `/v1/payments/refund-status/{refundId}` |
| Path Param | `refundId` — UUID của refund transaction |

#### Response (đề xuất) `200 OK`

```json
{
  "code": 200,
  "message": "Refund status retrieved",
  "result": {
    "refundId": "refund-uuid-...",
    "originalTransactionId": "txn-uuid-...",
    "orderId": "order-uuid-...",
    "refundAmount": 110000,
    "status": "SUCCESS",
    "reason": "Khách hàng hủy đơn",
    "createdAt": "2026-03-03T17:45:00Z",
    "completedAt": "2026-03-04T09:00:00Z"
  }
}
```

> **Ghi chú:** Hoàn tiền MoMo thường mất 5-7 ngày làm việc. Hệ thống cần polling/query MoMo refund status hoặc nhận callback.

---

## Sequence Diagrams

### Flow thanh toán MoMo (End-to-end)

```
Customer          FE              BE                    MoMo
   |               |               |                      |
   |--[Đặt hàng]-->|               |                      |
   |               |--POST /orders->|                      |
   |               |<-201 orderId---|                      |
   |               |               |                      |
   |--[Thanh toán]->|               |                      |
   |               |--POST /orders/{id}/payments---------->|
   |               |  {MOMO, amount, captureWallet}        |
   |               |               |--POST /v2/.../create->|
   |               |               |<------payUrl----------|
   |               |<--200 {payUrl}-|                      |
   |               |               |                      |
   |<-[redirect]---|               |                      |
   |------------[scan QR / pay on MoMo]------------------>|
   |               |               |                      |
   |               |               |<--POST /callback------|
   |               |               |  {resultCode=0}       |
   |               |               |--verify sig---------->|
   |               |               |--update Payment/Order |
   |               |               |--return {resultCode:0}|
   |               |               |                      |
   |<-----------[redirect to returnUrl]--------------------|
   |               |               |                      |
   |               |--GET /payments/{orderId}/status------->|
   |               |<--200 {status: PAID}-------------------|
   |               |               |                      |
   |<-["Cảm ơn!"]--|               |                      |
```

### Flow thanh toán CASH

```
Customer          FE              BE
   |               |               |
   |--[Đặt hàng]-->|               |
   |               |--POST /orders->|
   |               |<-201 orderId---|
   |               |               |
   |--[Thanh toán]->|               |
   |               |--POST /orders/{id}/payments
   |               |  {CASH, amount}
   |               |               |--set payment=PAID
   |               |               |--set order=PAID
   |               |<--200 {PAID}---|
   |               |               |
   |<-["Đã đặt!"]--|               |
```

---

## Error Code Reference

### Payment Errors (`30xxx`)

| Code | Error Key | Message | HTTP |
|------|-----------|---------|------|
| 30001 | `payment.failed` | Payment processing failed | 400 |
| 30002 | `payment.insufficient_funds` | Insufficient funds for the transaction | 400 |
| 30003 | `payment.transaction_not_found` | Transaction record not found | 404 |
| 30004 | `payment.invalid_method` | Invalid or unsupported payment method | 400 |
| 30005 | `payment.provider_error` | Error occurred while communicating with payment provider | 502 |
| 30006 | `payment.amount_mismatch` | Payment amount does not match order total | 400 |
| 404 | `payment.not_found` | Payment not found | 404 |
| 403 | `payment.access_denied` | Payment access denied | 403 |

### Order Errors (`20xxx`)

| Code | Error Key | Message | HTTP |
|------|-----------|---------|------|
| 404 | `order.not_found` | Order not found | 404 |
| 409 | `order.invalid_status` | Invalid order status transition | 400 |
| 403 | `order.not_owned` | User not owned this order | 410 |
| 20003 | `order.already_paid` | Order has already been paid | 409 |
| 20004 | `order.expired` | Order has expired | 410 |
| 20005 | `order.out_of_stock` | One or more products are out of stock | 400 |

### Common Errors (`2xxx` / `3xxx`)

| Code | Error Key | Message | HTTP |
|------|-----------|---------|------|
| 2000 | `common.bad_request` | Bad request | 400 |
| 2002 | `common.validation_failed` | Validation failed | 400 |
| 2003 | `common.resource_not_found` | Resource not found | 404 |
| 2005 | `common.forbidden` | Access forbidden | 403 |

---

## Database Schema (Payment-related tables)

```sql
-- payments
CREATE TABLE payments (
    id              UUID PRIMARY KEY,
    order_id        UUID NOT NULL REFERENCES orders(id),
    transaction_id  VARCHAR(100),          -- MoMo transId / external ref
    payment_method  VARCHAR(20) NOT NULL,  -- MOMO, CASH, VNPAY, PAYOS, WALLET
    status          VARCHAR(20) NOT NULL,  -- PENDING, PAID, FAILED, REFUNDED
    amount_paid     DECIMAL(12,2),
    error_message   VARCHAR(500),
    payment_date    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);

-- transactions (log chi tiết mỗi lần giao dịch)
CREATE TABLE transactions (
    id                  UUID PRIMARY KEY,
    payment_id          UUID NOT NULL REFERENCES payments(id),
    vnp_txn_ref         VARCHAR(255),      -- MoMo orderId / VNPay txn ref
    vnp_transaction_no  VARCHAR(255),      -- MoMo transId / VNPay transaction no
    vnp_response_code   VARCHAR(10),       -- MoMo resultCode / VNPay response code
    vnp_bank_code       VARCHAR(20),       -- MoMo payType / VNPay bank code
    type                VARCHAR(10) NOT NULL, -- PAYMENT, REFUND, TOPUP
    amount              DECIMAL(12,2) NOT NULL,
    status              VARCHAR(10) NOT NULL, -- PENDING, SUCCESS, FAILED
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL
);

-- orders
CREATE TABLE orders (
    id               UUID PRIMARY KEY,
    customer_id      UUID REFERENCES accounts(id),
    status           VARCHAR(20) NOT NULL,  -- PENDING, PAID, PREPARING, ...
    order_type       VARCHAR(50),
    order_number     VARCHAR(50),
    delivery_address VARCHAR(500),
    store_id         BIGINT,
    order_time       TIMESTAMP,
    total_amount     BIGINT,
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL
);

-- order_items
CREATE TABLE order_items (
    id                  UUID PRIMARY KEY,
    order_id            UUID REFERENCES orders(id),
    product_variant_id  UUID NOT NULL REFERENCES product_variants(id),
    quantity            INT NOT NULL,
    unit_price          DECIMAL(12,2) NOT NULL,
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL
);
```

---

## Cấu hình MoMo Sandbox

```properties
# application-dev.properties
momo.partner-code=MOMOLRJZ20181206
momo.access-key=mTCKt9W3eU1m39TW
momo.secret-key=SetA5RDnLHvt51AULf51DyauxUo3kDU6
momo.endpoint=https://test-payment.momo.vn/v2/gateway/api
momo.return-url=https://{ngrok-domain}/v1/payments/momo/return
momo.notify-url=https://{ngrok-domain}/v1/payments/momo/callback
```

> **Lưu ý:** Thay `{ngrok-domain}` bằng domain ngrok thực tế. Chạy `ngrok http 8080` trước khi test.

### Payment Methods (Config-driven)

```properties
# Thêm/sửa/tắt phương thức thanh toán chỉ cần sửa file này, không cần sửa Java code

# MOMO
payment.methods.momo.enabled=true
payment.methods.momo.name=MoMo
payment.methods.momo.description=Thanh to\u00e1n qua MoMo
payment.methods.momo.icon=momo
payment.methods.momo.sub-options.captureWallet.enabled=true
payment.methods.momo.sub-options.captureWallet.name=QR / V\u00ed MoMo
payment.methods.momo.sub-options.captureWallet.description=Qu\u00e9t m\u00e3 QR ho\u1eb7c m\u1edf app MoMo
payment.methods.momo.sub-options.payWithATM.enabled=false
payment.methods.momo.sub-options.payWithATM.name=Th\u1ebb ATM n\u1ed9i \u0111\u1ecba
payment.methods.momo.sub-options.payWithATM.description=Vietcombank, BIDV, MB...
payment.methods.momo.sub-options.payWithCC.enabled=false
payment.methods.momo.sub-options.payWithCC.name=Th\u1ebb qu\u1ed1c t\u1ebf
payment.methods.momo.sub-options.payWithCC.description=Visa, Mastercard, JCB

# CASH
payment.methods.cash.enabled=true
payment.methods.cash.name=Ti\u1ec1n m\u1eb7t
payment.methods.cash.description=Thanh to\u00e1n khi nh\u1eadn h\u00e0ng (COD)
payment.methods.cash.icon=cash

# VNPAY (disabled)
payment.methods.vnpay.enabled=false
payment.methods.vnpay.name=VNPay
# ...
```

> **Muốn bật `payWithATM`?** Sửa `payment.methods.momo.sub-options.payWithATM.enabled=true` và restart.

---

## Test Flow (Swagger)

### Bước 1 — Login lấy JWT cookie

```
POST /api/auth/login
Body: { "email": "alice@gmail.com", "password": "Password@123" }
→ Cookie JWT được set tự động
```

### Bước 2 — Tạo đơn hàng

```
POST /v1/orders
Body: {
  "items": [{ "productId": "00000000-0000-0000-0005-000000000002", "quantity": 2 }]
}
→ Ghi nhận orderId và totalAmount từ response
```

### Bước 3 — Thanh toán MoMo

```
POST /v1/orders/{orderId}/payments
Body: {
  "paymentMethod": "MOMO",
  "amount": {totalAmount},
  "momoRequestType": "captureWallet"
}
→ Copy paymentUrl, mở trong trình duyệt → quét QR bằng MoMo test app
```

### Bước 4 — Kiểm tra trạng thái

```
GET /v1/payments/{orderId}/status
→ status = "PAID" nếu đã thanh toán thành công
```
