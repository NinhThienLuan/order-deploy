## Order Service APIs

### 1. Create Order API

**1. Overview**
The user will provide a list of order items and delivery address to create a new order. The system will return the created order details.

**2. API Specification**
| HTTP Method | API URL | Permission |
| --- | --- | --- |
| POST | `/v1/orders` | Any Authenicated User |

**3. Request sample**
```json
{
  "items": [
    {
      "productVariantId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "quantity": 2
    }
  ],
  "deliveryAddress": "123 Main St"
}
```

| Field | Description | Data Type | Examples |
| --- | --- | --- | --- |
| items | List of order items | array | `[{"productVariantId": "...", "quantity": 2}]` |
| items[].productVariantId | UUID of the product variant | string (UUID) | `"3fa85f64-...-afa6"` |
| items[].quantity | Number of items | integer | `2` |
| deliveryAddress | Delivery address | string | `"123 Main St"` |

**4. Response sample**
```json
{
  "result": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "orderNumber": "ORD-123456",
    "customerId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "customerEmail": "tester@test.com",
    "status": "PENDING",
    "orderType": "DELIVERY",
    "orderTime": "2026-03-10T12:00:00",
    "deliveryAddress": "123 Main St",
    "totalAmount": 100.00,
    "items": [
      {
        "productId": "uuid",
        "productVariantId": "uuid",
        "productName": "Product",
        "variantSizeName": "M",
        "quantity": 2,
        "unitPrice": 50.00,
        "subtotal": 100.00
      }
    ]
  },
  "isSuccess": true,
  "statusCode": 201,
  "message": "Order created successfully"
}
```

**5. Validation**
| Status code | Description | Examples |
| --- | --- | --- |
| 400 | Validation failed (e.g. negative quantity) | `{"result": null, "isSuccess": false, "statusCode": 400, "message": "Order must contain at least one item"}` |

**6. Activity diagram**
/

**7. Sequence diagram**
/

---

### 2. Get My Orders API

**1. Overview**
Retrieve a paginated list of orders placed by the currently authenticated customer.

**2. API Specification**
| HTTP Method | API URL | Permission |
| --- | --- | --- |
| GET | `/v1/orders/me` | CUSTOMER |

**3. Request sample**
GET parameters:
- `page` (optional, default 1)
- `size` (optional, default 10)

| Field | Description | Data Type | Examples |
| --- | --- | --- | --- |
| page | Page number | integer | `1` |
| size | Page size | integer | `10` |

**4. Response sample**
```json
{
  "result": {
    "content": [
      {
        "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "orderNumber": "ORD-123",
        "status": "PENDING",
        "totalAmount": 100.00
      }
    ],
    "pageable": { ... },
    "totalElements": 1,
    "totalPages": 1
  },
  "isSuccess": true,
  "statusCode": 200,
  "message": "Get my orders successfully"
}
```

**5. Validation**
| Status code | Description | Examples |
| --- | --- | --- |
| 401 | Unauthorized / Invalid Token | `{"result": null, "isSuccess": false, "statusCode": 401, "message": "Unauthorized"}` |

**6. Activity diagram**
/

**7. Sequence diagram**
/

---

### 3. Get Order Detail API

**1. Overview**
Retrieve detailed information about a specific order by ID.

**2. API Specification**
| HTTP Method | API URL | Permission |
| --- | --- | --- |
| GET | `/v1/orders/{id}` | CUSTOMER, STORE_MANAGER, FRANCHISE_ADMIN |

**3. Request sample**
Path parameter: `id` (UUID of the order)

| Field | Description | Data Type | Examples |
| --- | --- | --- | --- |
| id | Order ID | string (UUID) | `"3fa85f64-..."` |

**4. Response sample**
```json
{
  "result": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "orderNumber": "ORD-123456",
    "status": "PENDING",
    ...
  },
  "isSuccess": true,
  "statusCode": 200,
  "message": "Get order detail successfully"
}
```

**5. Validation**
| Status code | Description | Examples |
| --- | --- | --- |
| 404 | Order not found or not owned by user | `{"result": null, "isSuccess": false, "statusCode": 404, "message": "Order not found"}` |

**6. Activity diagram**
/

**7. Sequence diagram**
/

---

### 4. Cancel Order API

**1. Overview**
Cancel an existing order.

**2. API Specification**
| HTTP Method | API URL | Permission |
| --- | --- | --- |
| PATCH | `/v1/orders/{id}/cancel` | Any Authenticated User (Owner) |

**3. Request sample**
Path parameter: `id` (UUID of the order)

**4. Response sample**
```json
{
  "result": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "status": "CANCELED"
  },
  "isSuccess": true,
  "statusCode": 200,
  "message": "Order cancelled"
}
```

**5. Validation**
| Status code | Description | Examples |
| --- | --- | --- |
| 400 | Order cannot be cancelled | `{"result": null, "isSuccess": false, "statusCode": 400, "message": "Order cannot be cancelled in current status"}` |

**6. Activity diagram**
/

**7. Sequence diagram**
/

---

### 5. Get Order History (Admin/Manager) API

**1. Overview**
Retrieve order history with filtering by status and date range for administrative users.

**2. API Specification**
| HTTP Method | API URL | Permission |
| --- | --- | --- |
| GET | `/v1/orders` | FRANCHISE_ADMIN, STORE_MANAGER |

**3. Request sample**
GET parameters:
- `page`, `size`
- `status` (optional)
- `fromDate`, `toDate` (optional ISO dates)

**4. Response sample**
```json
{
  "result": {
     "content": [ ... ]
  },
  "isSuccess": true,
  "statusCode": 200,
  "message": "Get order history successfully"
}
```

**5. Validation**
| Status code | Description | Examples |
| --- | --- | --- |
| 403 | Forbidden | `{"result": null, "isSuccess": false, "statusCode": 403, "message": "Forbidden"}` |

**6. Activity diagram**
/

**7. Sequence diagram**
/

---

### 6. Get Order Statuses API

**1. Overview**
Retrieve a list of all possible order statuses.

**2. API Specification**
| HTTP Method | API URL | Permission |
| --- | --- | --- |
| GET | `/v1/orders/statuses` | N/A |

**3. Request sample**
N/A

**4. Response sample**
```json
{
  "result": [
    "PENDING",
    "CONFIRMED",
    "PREPARING",
    "READY",
    "DELIVERING",
    "PAID",
    "COMPLETED",
    "CANCELED",
    "REFUNDED"
  ],
  "isSuccess": true,
  "statusCode": 200,
  "message": "Get order statuses successfully"
}
```

**5. Validation**
N/A

**6. Activity diagram**
/

**7. Sequence diagram**
/

---

