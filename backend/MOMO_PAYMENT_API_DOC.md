# MoMo Payment API Documentation

---

## **1. Process Payment (MoMo)**

1. **Overview**

   - Khách hàng gửi yêu cầu thanh toán đơn hàng qua MoMo. Hệ thống sẽ tạo link thanh toán MoMo và trả về `paymentUrl` để FE redirect người dùng sang trang thanh toán MoMo.

2. **API Specification**

   | **API**    | **URL**                     |
   |------------|-----------------------------|
   | POST       | /v1/orders/{id}/payments    |
   | Permission | CUSTOMER (JWT required)     |

3. **Request sample**

   **Path Parameter:**

   | **Field** | **Description**            | **Data Type** | **Examples**                         |
   |-----------|----------------------------|---------------|--------------------------------------|
   | id        | Order ID (UUID)            | UUID          | 3fa85f64-5717-4562-b3fc-2c963f66afa6 |

   **Request Body:**

   ```json
   {
     "paymentMethod": "MOMO",
     "amount": 150000,
     "momoRequestType": "captureWallet"
   }
   ```

   | **Field**        | **Description**                                                                 | **Data Type** | **Required** | **Examples**                          |
   |------------------|---------------------------------------------------------------------------------|---------------|--------------|---------------------------------------|
   | paymentMethod    | Phương thức thanh toán                                                          | string        | Yes          | MOMO                                  |
   | amount           | Số tiền thanh toán (phải khớp với tổng đơn hàng)                                 | number        | Yes          | 150000                                |
   | momoRequestType  | Kênh thanh toán MoMo (mặc định: `captureWallet`)                                | string        | No           | captureWallet, payWithATM, payWithCC  |

   **Các giá trị `momoRequestType` hỗ trợ:**

   | **Value**       | **Tên hiển thị**      | **Mô tả**                             |
   |-----------------|-----------------------|---------------------------------------|
   | captureWallet   | QR / Ví MoMo          | Quét mã QR hoặc mở app MoMo          |
   | payWithATM      | Thẻ ATM nội địa       | Vietcombank, BIDV, MB, Techcombank... |
   | payWithCC       | Thẻ quốc tế           | Visa, Mastercard, JCB                 |

4. **Response sample**

   ```json
   {
     "code": 200,
     "message": "Success",
     "result": {
       "paymentId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
       "orderId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
       "paymentMethod": "MOMO",
       "amountPaid": 150000,
       "paymentStatus": "PENDING",
       "orderStatus": "PENDING",
       "paymentUrl": "https://test-payment.momo.vn/v2/gateway/pay?t=...",
       "expiredAt": "2026-03-04T15:30:00",
       "createdAt": "2026-03-04T15:00:00"
     },
     "isSuccess": true,
     "statusCode": 200,
     "timestamp": "2026-03-04T15:00:00Z"
   }
   ```

   | **Field**       | **Description**                                          | **Data Type** |
   |-----------------|----------------------------------------------------------|---------------|
   | paymentId       | ID của bản ghi thanh toán                                | UUID          |
   | orderId         | ID của đơn hàng                                          | UUID          |
   | paymentMethod   | Phương thức thanh toán                                   | string        |
   | amountPaid      | Số tiền thanh toán                                       | number        |
   | paymentStatus   | Trạng thái thanh toán (PENDING, PAID, FAILED, REFUNDED)  | string        |
   | orderStatus     | Trạng thái đơn hàng                                      | string        |
   | paymentUrl      | URL redirect sang trang MoMo (chỉ có khi MOMO)           | string        |
   | expiredAt       | Thời gian hết hạn thanh toán                              | datetime      |
   | createdAt       | Thời gian tạo thanh toán                                  | datetime      |

5. **Validation**

   | **Status Code** | **Description**                            | **Examples**                                                                                 |
   |-----------------|--------------------------------------------|----------------------------------------------------------------------------------------------|
   | 400             | Payment amount does not match order total  | `{ "result": null, "isSuccess": false, "statusCode": 400, "message": "Payment amount does not match order total" }` |
   | 400             | Invalid or unsupported payment method      | `{ "result": null, "isSuccess": false, "statusCode": 400, "message": "Invalid or unsupported payment method" }` |
   | 400             | Payment method MOMO: payWithATM is disabled | `{ "result": null, "isSuccess": false, "statusCode": 400, "message": "MoMo request type payWithATM is not enabled" }` |
   | 404             | Order not found                            | `{ "result": null, "isSuccess": false, "statusCode": 404, "message": "Order not found" }` |
   | 502             | Error communicating with MoMo              | `{ "result": null, "isSuccess": false, "statusCode": 502, "message": "Error occurred while communicating with payment provider" }` |

6. **Activity diagram**

   ```
   [Khách hàng] ---> POST /v1/orders/{id}/payments (paymentMethod=MOMO)
        |
        v
   [Server] Validate JWT token, lấy customerId
        |
        v
   [Server] Kiểm tra đơn hàng tồn tại, thuộc về customer, status = PENDING
        |
        v
   [Server] Tính tổng tiền đơn hàng, kiểm tra khớp amount
        |
        v
   [Server] Kiểm tra momoRequestType có được bật trong config
        |
        v
   [Server] Tạo/tái sử dụng PaymentEntity (status = PENDING)
        |
        v
   [Server] Gọi MoMo API /v2/gateway/api/create → nhận payUrl
        |
        v
   [Server] Trả về PaymentResponseDTO với paymentUrl
        |
        v
   [FE] Redirect người dùng tới paymentUrl (trang MoMo)
   ```

7. **Sequence diagram**

   ```
   Customer      FE              Server           MoMo API
      |           |                 |                 |
      |--chọn MoMo-->|             |                 |
      |           |--POST /v1/orders/{id}/payments-->|
      |           |                 |--validate order--|
      |           |                 |--POST /create-->|
      |           |                 |<--payUrl--------|
      |           |<--paymentUrl----|                 |
      |<--redirect--|               |                 |
      |----------thanh toán trên MoMo--------------->|
      |           |                 |<--IPN callback--|
      |           |                 |--update status--|
      |<--redirect về returnUrl-----|                 |
      |           |--GET /momo/return-->|             |
      |           |<--payment result---|             |
   ```

---

## **2. MoMo IPN Callback**

1. **Overview**

   - Sau khi khách hàng thanh toán trên MoMo, MoMo server gọi endpoint này (server-to-server) để thông báo kết quả thanh toán. Đây là nguồn xác thực chính thức cho trạng thái thanh toán.

2. **API Specification**

   | **API**    | **URL**                          |
   |------------|----------------------------------|
   | POST       | /v1/payments/momo/callback       |
   | Permission | N/A (Public — MoMo server gọi)   |

3. **Request sample**

   MoMo gửi request body dạng JSON:

   ```json
   {
     "partnerCode": "MOMOLRJZ20181206",
     "orderId": "MOMO-3fa85f64-5717-4562-b3fc-2c963f66afa6-1709550000000",
     "requestId": "req-uuid-123",
     "amount": "150000",
     "orderInfo": "Payment for order 3fa85f64-...",
     "orderType": "momo_wallet",
     "transId": "2709550012345",
     "resultCode": "0",
     "message": "Successful.",
     "payType": "qr",
     "responseTime": "1709550060000",
     "extraData": "eyJvcmRlcklkIjoiM2ZhODVmNjQtNTcxNy00NTYyLWIzZmMtMmM5NjNmNjZhZmE2In0=",
     "signature": "abc123def456..."
   }
   ```

   | **Field**      | **Description**                                          | **Data Type** | **Examples**                                  |
   |----------------|----------------------------------------------------------|---------------|-----------------------------------------------|
   | partnerCode    | Mã đối tác MoMo                                         | string        | MOMOLRJZ20181206                              |
   | orderId        | Mã đơn hàng MoMo (format: MOMO-{uuid}-{timestamp})      | string        | MOMO-3fa85f64-...-1709550000000               |
   | requestId      | Request ID duy nhất                                       | string        | req-uuid-123                                  |
   | amount         | Số tiền thanh toán                                       | string        | 150000                                        |
   | transId        | Mã giao dịch MoMo                                       | string        | 2709550012345                                 |
   | resultCode     | Mã kết quả (0 = thành công)                               | string        | 0                                             |
   | message        | Thông báo kết quả                                         | string        | Successful.                                   |
   | payType        | Loại thanh toán (qr, napas, credit)                       | string        | qr                                            |
   | extraData      | Dữ liệu bổ sung (Base64 JSON chứa orderId hệ thống)      | string        | eyJvcmRlcklkIjoiLi4uIn0=                     |
   | signature      | Chữ ký HMAC-SHA256 để xác thực                            | string        | abc123def456...                               |

4. **Response sample**

   **Thành công (resultCode = 0):**

   ```json
   {
     "resultCode": 0,
     "message": "OK"
   }
   ```

   **Signature không hợp lệ:**

   ```json
   {
     "resultCode": 1,
     "message": "Invalid signature"
   }
   ```

   **extraData không hợp lệ:**

   ```json
   {
     "resultCode": 1,
     "message": "Invalid extraData"
   }
   ```

   | **Field**   | **Description**                    | **Data Type** |
   |-------------|------------------------------------|---------------|
   | resultCode  | 0 = đã xử lý, 1 = lỗi             | number        |
   | message     | Thông báo kết quả xử lý            | string        |

5. **Validation**

   | **Trường hợp**                  | **Xử lý**                                                  |
   |---------------------------------|-------------------------------------------------------------|
   | Signature không hợp lệ          | Trả về `resultCode: 1`, không cập nhật gì                   |
   | extraData không parse được       | Trả về `resultCode: 1`                                      |
   | Không tìm thấy payment PENDING  | Trả về `resultCode: 0` (để MoMo không retry)                |
   | resultCode = 0 (thành công)     | Cập nhật Payment → PAID, Order → PAID, tạo Transaction      |
   | resultCode ≠ 0 (thất bại)      | Cập nhật Payment → FAILED, tạo Transaction với FAILED status |

6. **Activity diagram**

   ```
   [MoMo Server] ---> POST /v1/payments/momo/callback
        |
        v
   [Server] Xác thực HMAC-SHA256 signature
        |--- Không hợp lệ --> trả về resultCode: 1
        |
        v
   [Server] Decode extraData (Base64) → lấy orderId
        |--- Parse lỗi --> trả về resultCode: 1
        |
        v
   [Server] Tìm PaymentEntity PENDING theo orderId
        |--- Không tìm thấy --> trả về resultCode: 0
        |
        v
   [Server] Tạo TransactionEntity
        |
        v
   [Server] resultCode == 0?
        |--- Có --> Payment = PAID, Order = PAID
        |--- Không --> Payment = FAILED
        |
        v
   [Server] Trả về resultCode: 0, message: "OK"
   ```

7. **Sequence diagram**

   ```
   MoMo Server            Server                   DB
       |                    |                        |
       |--POST /callback-->|                         |
       |                    |--verify signature------>|
       |                    |--decode extraData------>|
       |                    |--find PENDING payment-->|
       |                    |<--PaymentEntity---------|
       |                    |--save Transaction------>|
       |                    |--update Payment-------->|
       |                    |--update Order---------->|
       |<--{resultCode:0}--|                          |
   ```

---

## **3. MoMo Return (Redirect)**

1. **Overview**

   - Sau khi thanh toán xong trên MoMo, người dùng được redirect về endpoint này. Server xác thực signature và trả về kết quả thanh toán để FE hiển thị.

2. **API Specification**

   | **API**    | **URL**                        |
   |------------|--------------------------------|
   | GET        | /v1/payments/momo/return       |
   | Permission | N/A (Public — redirect từ MoMo) |

3. **Request sample**

   MoMo redirect với query parameters:

   ```
   GET /v1/payments/momo/return?partnerCode=MOMOLRJZ20181206&orderId=MOMO-3fa85f64-...-1709550000000&requestId=req-uuid-123&amount=150000&orderInfo=Payment+for+order&orderType=momo_wallet&transId=2709550012345&resultCode=0&message=Successful.&payType=qr&responseTime=1709550060000&extraData=eyJ...&signature=abc123...
   ```

   | **Field**      | **Description**                          | **Data Type** | **Examples**             |
   |----------------|------------------------------------------|---------------|--------------------------|
   | partnerCode    | Mã đối tác MoMo                         | string        | MOMOLRJZ20181206         |
   | orderId        | Mã đơn hàng MoMo                        | string        | MOMO-3fa85f64-...-...    |
   | amount         | Số tiền                                  | string        | 150000                   |
   | transId        | Mã giao dịch MoMo                       | string        | 2709550012345            |
   | resultCode     | Mã kết quả (0 = thành công)              | string        | 0                        |
   | message        | Thông báo kết quả                        | string        | Successful.              |
   | payType        | Loại thanh toán                          | string        | qr                       |
   | signature      | Chữ ký HMAC-SHA256                       | string        | abc123...                |

4. **Response sample**

   **Thanh toán thành công:**

   ```json
   {
     "success": true,
     "resultCode": 0,
     "message": "Successful.",
     "momoOrderId": "MOMO-3fa85f64-5717-4562-b3fc-2c963f66afa6-1709550000000",
     "transId": "2709550012345",
     "amount": "150000"
   }
   ```

   **Thanh toán thất bại (người dùng hủy):**

   ```json
   {
     "success": false,
     "resultCode": 1006,
     "message": "Transaction denied by user.",
     "momoOrderId": "MOMO-3fa85f64-5717-4562-b3fc-2c963f66afa6-1709550000000",
     "transId": null,
     "amount": "150000"
   }
   ```

   | **Field**    | **Description**                                    | **Data Type** |
   |--------------|----------------------------------------------------|---------------|
   | success      | true nếu signature hợp lệ VÀ resultCode = 0        | boolean       |
   | resultCode   | Mã kết quả từ MoMo                                  | number        |
   | message      | Thông báo từ MoMo                                   | string        |
   | momoOrderId  | Mã đơn hàng phía MoMo                               | string        |
   | transId      | Mã giao dịch MoMo (null nếu thất bại)               | string        |
   | amount       | Số tiền                                              | string        |

5. **Validation**

   | **Status Code** | **Description**                     | **Examples**                                                                 |
   |-----------------|-------------------------------------|------------------------------------------------------------------------------|
   | 200             | Signature hợp lệ, thanh toán thành công | `{ "success": true, "resultCode": 0, "message": "Successful." }`            |
   | 200             | Signature không hợp lệ hoặc thất bại    | `{ "success": false, "resultCode": 1006, "message": "Transaction denied by user." }` |

   > **Lưu ý:** Endpoint này luôn trả HTTP 200. FE dựa vào field `success` để xác định kết quả.

6. **Activity diagram**

   ```
   [MoMo] --redirect--> GET /v1/payments/momo/return?params...
        |
        v
   [Server] Xác thực HMAC-SHA256 signature
        |
        v
   [Server] Đọc resultCode từ query params
        |
        v
   [Server] success = (signature hợp lệ AND resultCode == 0)
        |
        v
   [Server] Trả về JSON { success, resultCode, message, momoOrderId, transId, amount }
        |
        v
   [FE] Hiển thị kết quả thanh toán cho người dùng
   ```

7. **Sequence diagram**

   ```
   Customer        MoMo            Server              FE
      |              |                |                  |
      |--hoàn tất--->|                |                  |
      |              |--redirect GET-->|                 |
      |              |                |--verify sig----->|
      |              |                |--build result--->|
      |              |<--JSON result---|                 |
      |<--redirect to FE payment result page----------->|
      |              |                |                  |--show result
   ```

---

## **4. Get Payment Methods**

1. **Overview**

   - Lấy danh sách các phương thức thanh toán được hỗ trợ (bao gồm MoMo sub-options). Dùng để FE hiển thị lựa chọn phương thức thanh toán.

2. **API Specification**

   | **API**    | **URL**                    |
   |------------|----------------------------|
   | GET        | /v1/payments/methods       |
   | Permission | N/A (Public)               |

3. **Request sample**

   Không có request body hay query parameter.

   ```
   GET /v1/payments/methods
   ```

4. **Response sample**

   ```json
   {
     "code": 200,
     "message": "Success",
     "result": [
       {
         "code": "MOMO",
         "name": "Ví MoMo",
         "description": "Thanh toán qua ví điện tử MoMo",
         "icon": "momo-icon",
         "enabled": true,
         "subOptions": [
           {
             "code": "captureWallet",
             "name": "QR / Ví MoMo",
             "description": "Quét mã QR hoặc mở app MoMo",
             "enabled": true
           },
           {
             "code": "payWithATM",
             "name": "Thẻ ATM nội địa",
             "description": "Vietcombank, BIDV, MB, Techcombank...",
             "enabled": true
           },
           {
             "code": "payWithCC",
             "name": "Thẻ quốc tế",
             "description": "Visa, Mastercard, JCB",
             "enabled": true
           }
         ]
       },
       {
         "code": "CASH",
         "name": "Tiền mặt",
         "description": "Thanh toán khi nhận hàng",
         "icon": "cash-icon",
         "enabled": true,
         "subOptions": []
       }
     ],
     "timestamp": "2026-03-04T15:00:00Z"
   }
   ```

   | **Field**              | **Description**                     | **Data Type** |
   |------------------------|-------------------------------------|---------------|
   | code                   | Mã phương thức thanh toán           | string        |
   | name                   | Tên hiển thị                        | string        |
   | description            | Mô tả                              | string        |
   | icon                   | Tên icon                            | string        |
   | enabled                | Có đang bật hay không               | boolean       |
   | subOptions             | Danh sách kênh con (chỉ MoMo có)    | array         |
   | subOptions[].code      | Mã kênh con (= momoRequestType)     | string        |
   | subOptions[].name      | Tên kênh con                        | string        |
   | subOptions[].description | Mô tả kênh con                    | string        |
   | subOptions[].enabled   | Kênh con có đang bật không           | boolean       |

5. **Validation**

   Không có validation đặc biệt. Luôn trả HTTP 200.

---

## **5. Get Payment Status**

1. **Overview**

   - Kiểm tra trạng thái thanh toán của một đơn hàng, bao gồm thông tin giao dịch MoMo.

2. **API Specification**

   | **API**    | **URL**                          |
   |------------|----------------------------------|
   | GET        | /v1/payments/{orderId}/status    |
   | Permission | CUSTOMER (JWT required)          |

3. **Request sample**

   | **Field** | **Description** | **Data Type** | **Examples**                         |
   |-----------|-----------------|---------------|--------------------------------------|
   | orderId   | Order ID (UUID) | UUID          | 3fa85f64-5717-4562-b3fc-2c963f66afa6 |

   ```
   GET /v1/payments/3fa85f64-5717-4562-b3fc-2c963f66afa6/status
   ```

4. **Response sample**

   ```json
   {
     "code": 200,
     "message": "Success",
     "result": {
       "orderId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
       "paymentId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
       "paymentMethod": "MOMO",
       "status": "PAID",
       "amountPaid": 150000,
       "transaction": {
         "vnpTxnRef": "MOMO-3fa85f64-...-1709550000000",
         "vnpTransactionNo": "2709550012345",
         "vnpResponseCode": "0",
         "vnpBankCode": "qr"
       }
     },
     "timestamp": "2026-03-04T15:05:00Z"
   }
   ```

   | **Field**                     | **Description**                                         | **Data Type** |
   |-------------------------------|---------------------------------------------------------|---------------|
   | orderId                       | ID đơn hàng                                              | UUID          |
   | paymentId                     | ID bản ghi thanh toán                                    | UUID          |
   | paymentMethod                 | Phương thức thanh toán                                   | string        |
   | status                        | Trạng thái (PENDING, PAID, FAILED, REFUNDED)             | string        |
   | amountPaid                    | Số tiền                                                  | number        |
   | transaction.vnpTxnRef         | Mã đơn hàng MoMo (MOMO-{uuid}-{timestamp})              | string        |
   | transaction.vnpTransactionNo  | Mã giao dịch MoMo                                        | string        |
   | transaction.vnpResponseCode   | Mã kết quả từ MoMo (0 = thành công)                      | string        |
   | transaction.vnpBankCode       | Loại thanh toán (qr, napas, credit, MOMO)                 | string        |

5. **Validation**

   | **Status Code** | **Description**      | **Examples**                                                                           |
   |-----------------|----------------------|----------------------------------------------------------------------------------------|
   | 404             | Payment not found    | `{ "result": null, "isSuccess": false, "statusCode": 404, "message": "Payment not found" }` |
   | 403             | Payment access denied | `{ "result": null, "isSuccess": false, "statusCode": 403, "message": "Payment access denied" }` |

---

## **6. Get Payment History**

1. **Overview**

   - Lấy danh sách lịch sử thanh toán của khách hàng hiện tại.

2. **API Specification**

   | **API**    | **URL**            |
   |------------|--------------------|
   | GET        | /v1/payments       |
   | Permission | CUSTOMER (JWT required) |

3. **Request sample**

   ```
   GET /v1/payments
   Authorization: Bearer <accessToken>
   ```

4. **Response sample**

   ```json
   {
     "code": 200,
     "message": "Success",
     "result": [
       {
         "paymentId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
         "orderId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
         "paymentMethod": "MOMO",
         "amountPaid": 150000,
         "paymentStatus": "PAID",
         "orderStatus": "PAID",
         "createdAt": "2026-03-04T15:00:00"
       }
     ],
     "timestamp": "2026-03-04T15:05:00Z"
   }
   ```

5. **Validation**

   | **Status Code** | **Description**  | **Examples**                                                                             |
   |-----------------|------------------|------------------------------------------------------------------------------------------|
   | 401             | Unauthorized     | JWT token thiếu hoặc không hợp lệ                                                        |

---

## **Error Codes Reference**

| **Code** | **Description**                                      | **HTTP Status** |
|----------|------------------------------------------------------|-----------------|
| 30001    | Payment processing failed                            | 400             |
| 30002    | Insufficient funds for the transaction               | 400             |
| 30003    | Transaction record not found                         | 404             |
| 30004    | Invalid or unsupported payment method                | 400             |
| 30005    | Error occurred while communicating with payment provider | 502          |
| 30006    | Payment amount does not match order total             | 400             |
| 404      | Payment not found                                    | 404             |
| 403      | Payment access denied                                | 403             |

---

## **MoMo Result Codes (Tham khảo)**

| **resultCode** | **Ý nghĩa**                    |
|----------------|---------------------------------|
| 0              | Giao dịch thành công            |
| 9000           | Giao dịch đã được xác nhận      |
| 1001           | Giao dịch thất bại do thiếu tiền |
| 1002           | Giao dịch bị từ chối bởi nguồn tiền |
| 1003           | Giao dịch bị hủy                |
| 1004           | Số tiền vượt hạn mức            |
| 1005           | URL hoặc QR code hết hạn        |
| 1006           | Người dùng từ chối xác nhận     |

---

## **MoMo Sandbox Test Config**

| **Property**     | **Value (Dev)**                                              |
|------------------|--------------------------------------------------------------|
| Partner Code     | MOMOLRJZ20181206                                             |
| Access Key       | mTCKt9W3eU1m39TW                                            |
| Endpoint         | https://test-payment.momo.vn/v2/gateway/api                 |
| Return URL       | http://localhost:3000/payment/result                         |
| Notify URL (IPN) | Cần dùng ngrok hoặc tunnel để MoMo gọi được localhost        |

---

## **Full Payment Flow Diagram**

```
┌──────────┐     ┌──────────┐     ┌──────────────┐     ┌──────────┐
│ Customer │     │    FE    │     │   Backend    │     │ MoMo API │
└────┬─────┘     └────┬─────┘     └──────┬───────┘     └────┬─────┘
     │                │                   │                   │
     │  1. Chọn MoMo  │                   │                   │
     │───────────────>│                   │                   │
     │                │  2. POST /orders/{id}/payments        │
     │                │──────────────────>│                   │
     │                │                   │  3. POST /create  │
     │                │                   │──────────────────>│
     │                │                   │  4. payUrl        │
     │                │                   │<──────────────────│
     │                │  5. paymentUrl    │                   │
     │                │<──────────────────│                   │
     │  6. Redirect   │                   │                   │
     │<───────────────│                   │                   │
     │                │                   │                   │
     │  7. Thanh toán trên MoMo          │                   │
     │───────────────────────────────────────────────────────>│
     │                │                   │                   │
     │                │                   │  8. IPN callback  │
     │                │                   │<──────────────────│
     │                │                   │  9. Update DB     │
     │                │                   │──────┐            │
     │                │                   │<─────┘            │
     │                │                   │  10. {resultCode:0}│
     │                │                   │──────────────────>│
     │                │                   │                   │
     │  11. Redirect về returnUrl         │                   │
     │<───────────────────────────────────────────────────────│
     │                │  12. GET /momo/return                 │
     │                │──────────────────>│                   │
     │                │  13. JSON result  │                   │
     │                │<──────────────────│                   │
     │  14. Hiển thị  │                   │                   │
     │<───────────────│                   │                   │
```
