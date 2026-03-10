## Payment Service APIs

### 1. Create Payment API

**1. Overview**
Create a new payment for a specified order with a chosen payment method.

**2. API Specification**
| HTTP Method | API URL | Permission |
| --- | --- | --- |
| POST | `/api/v1/payments` | N/A |

**3. Request sample**
```json
{
  "orderId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "paymentMethod": "VNPAY",
  "amount": 100.00,
  "transactionId": "txn-123",
  "momoRequestType": "captureWallet"
}
```

| Field | Description | Data Type | Examples |
| --- | --- | --- | --- |
| orderId | UUID of the order | string (UUID) | `"3fa85f64-..."` |
| paymentMethod | Method of payment | string | `"VNPAY", "MOMO", "CASH"` |
| amount | Amount to be paid | number | `100.00` |
| transactionId | Optional transaction ID | string | `"txn-123"` |
| momoRequestType | Optional momo payment type | string | `"captureWallet"` |

**4. Response sample**
```json
{
  "result": {
    "paymentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "orderId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "paymentMethod": "VNPAY",
    "amountPaid": 100.00,
    "paymentStatus": "PENDING",
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...",
    "expiredAt": "2026-03-10T12:15:00",
    "createdAt": "2026-03-10T12:00:00"
  },
  "isSuccess": true,
  "statusCode": 201,
  "message": "Payment created"
}
```

**5. Validation**
| Status code | Description | Examples |
| --- | --- | --- |
| 400 | Invalid data (e.g. amount < 0.01) | `{"result": null, "isSuccess": false, "statusCode": 400, "message": "Invalid amount"}` |

**6. Activity diagram**

**7. Sequence diagram**
/

⚠️ Warning

transactionId là idempotency key — nếu gửi lại cùng transactionId, hệ thống trả về payment đã tồn tại thay vì tạo mới.

paymentUrl và qrCode không đồng thời có giá trị — một trong hai sẽ là null tùy phương thức thanh toán.

Payment sẽ tự động expire sau 15 phút nếu không có webhook xác nhận từ gateway.

CUSTOMER chỉ được tạo payment cho order của chính mình — trả 403 nếu orderId thuộc order người khác.

Không được gọi API này nhiều lần cho cùng một order nếu đã có payment ở trạng thái Pending — trả 409 Conflict.
---

### 2. Get Payment Status API

**1. Overview**
Retrieve the current status of a payment by the associated order ID.

**2. API Specification**
| HTTP Method | API URL | Permission |
| --- | --- | --- |
| GET | `/api/v1/payments/order/{orderId}/status` | N/A |

**3. Request sample**
Path parameter: `orderId` (UUID)

**4. Response sample**
```json
{
  "result": {
    "paymentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "orderId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "paymentMethod": "VNPAY",
    "amountPaid": 100.00,
    "paymentStatus": "PAID"
  },
  "isSuccess": true,
  "statusCode": 200,
  "message": "Get payment status"
}
```

**5. Validation**
| Status code | Description | Examples |
| --- | --- | --- |
| 404 | Payment not found | `{"result": null, "isSuccess": false, "statusCode": 404, "message": "Payment not found"}` |

**6. Activity diagram**
/

**7. Sequence diagram**
/

---

### 3. Process VNPay Webhook API

**1. Overview**
Handle incoming IPN / webhook callbacks from VNPay.

**2. API Specification**
| HTTP Method | API URL | Permission |
| --- | --- | --- |
| POST | `/api/v1/payments/webhook` | N/A |

**3. Request sample**
Request params: VNPay parameters (vnp_TxnRef, vnp_ResponseCode, etc.)

**4. Response sample**
```json
{
  "result": {
    "code": "00",
    "message": "Confirm Success"
  },
  "isSuccess": true,
  "statusCode": 200,
  "message": "Webhook processed"
}
```

**5. Validation**
N/A

**6. Activity diagram**
/

**7. Sequence diagram**
/

---

### 4. VNPay Return API

**1. Overview**
Handle synchronous return callback from VNPay after user completes the flow.

**2. API Specification**
| HTTP Method | API URL | Permission |
| --- | --- | --- |
| GET | `/api/v1/payments/vnpay-return` | N/A |

**3. Request sample**
Request params: VNPay parameters

**4. Response sample**
```json
{
  "result": {
    "code": "00",
    "message": "Confirm Success"
  },
  "isSuccess": true,
  "statusCode": 200,
  "message": "VNPay return processed"
}
```

**5. Validation**
N/A

**6. Activity diagram**
/

**7. Sequence diagram**
/

---

### 5. MoMo Callback API

**1. Overview**
Handle incoming async callbacks from MoMo.

**2. API Specification**
| HTTP Method | API URL | Permission |
| --- | --- | --- |
| POST | `/api/v1/payments/momo/callback` | N/A |

**3. Request sample**
JSON Body: MoMo parameters

**4. Response sample**
Returns NO OP or basic map indicating success to MoMo immediately.
```json
{
  "status": 200,
  "message": "success"
}
```

**5. Validation**
N/A

**6. Activity diagram**
/

**7. Sequence diagram**
/

---

### 6. MoMo Return API

**1. Overview**
Handle synchronous return callback from MoMo and redirect to frontend.

**2. API Specification**
| HTTP Method | API URL | Permission |
| --- | --- | --- |
| GET | `/api/v1/payments/momo/return` | N/A |

**3. Request sample**
Query params: MoMo parameters

**4. Response sample**
HTTP 302 Redirect to the result URL.

**5. Validation**
N/A

**6. Activity diagram**
/

**7. Sequence diagram**
/
