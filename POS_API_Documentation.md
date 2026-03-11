# POS API Documentation

**Version:** v1  
**Description:** Endpoints which are operating with myPOS Terminals

## Servers

- **Demo:** https://demo-api-gateway.mypos.com
- **Production:** https://api-gateway.mypos.com

## Authentication

This API uses Bearer token authentication (OAuth with JWT).

**Security Scheme:**
- Type: HTTP
- Scheme: oauth
- Bearer Format: JWT

**Required Headers:**
- `X-Session`: Session token (required)
- `X-Partner-Id`: Partner identifier (required)
- `X-Application-Id`: Application identifier (required)

---

## Endpoints

### Payment Operations

#### 1. Create Payment

**POST** `/epos/v1/payments`

Initiate a payment on a POS device.

**Headers:**
- `X-Session` (required): string
- `X-Partner-Id` (required): string
- `X-Application-Id` (required): string

**Request Body:**

```json
{
  "reference_number": "REF1234567890",
  "amount": {
    "value": 1500,
    "currency_code": "EUR",
    "tip": 500
  },
  "description": "Test request",
  "terminal_id": "80123457",
  "app_name": "Test123",
  "app_version": "12",
  "operator_code": "0123"
}
```

**Response (201 - Created):**

```json
{
  "payment_id": "68ff25a853659f6448da0eba",
  "status": "InProgress",
  "amount": {
    "value": 1500,
    "currency_code": "EUR",
    "tip": 500
  },
  "created_at": "2025-10-27T09:56:24",
  "updated_at": "2025-10-27T09:56:24",
  "reference_number": "REF1234567890",
  "description": "Test request",
  "expire_at": "2025-11-27T09:56:24"
}
```

**Error Responses:**
- `400` Bad Request
- `401` Unauthorized
- `403` Forbidden
- `404` Not Found
- `500` Internal Server Error

---

#### 2. Get Payment By ID

**GET** `/epos/v1/payments/{id}`

Check the status of a previously initiated payment and get the details.

**Path Parameters:**
- `id` (required): string - Payment ID

**Headers:**
- `X-Session` (required): string
- `X-Partner-Id` (required): string
- `X-Application-Id` (required): string

**Response (200 - OK):**

```json
{
  "payment_id": "67f826f21b62d43a1f65833a",
  "status": "InProgress",
  "amount": {
    "value": 1500,
    "currency_code": "EUR",
    "tip": 500
  },
  "created_at": "2025-10-27T07:35:48",
  "updated_at": "2025-10-27T07:35:48",
  "reference_number": "REF1234567890",
  "description": "Test request",
  "expire_at": "2025-11-26T07:35:48",
  "dcc_amount": 0,
  "dcc_card_exchange_rate": 0,
  "stan": "000408",
  "auth_code": "VISSIM",
  "transaction_date_local": "250620165140",
  "status_message": "TRANSACTION_SUCCESS",
  "dcc_available": false,
  "reference_type": "OFF",
  "signature_required": false,
  "aid": "A0000000031010",
  "card_qualifier": "VISA",
  "response_code": "00",
  "pan": "****6693",
  "rrn": "string"
}
```

**Error Responses:**
- `400` Bad Request
- `401` Unauthorized
- `403` Forbidden
- `404` Not Found
- `500` Internal Server Error

---

#### 3. Get Terminal Payments By TID

**GET** `/epos/v1/payments`

Get paginated list of payments for a specific terminal.

**Query Parameters:**
- `terminal_id` (required): string - Terminal identifier
- `page`: integer - Page number
- `size`: integer - Page size

**Headers:**
- `X-Session` (required): string
- `X-Partner-Id` (required): string
- `X-Application-Id` (required): string

**Response (200 - OK):**

```json
{
  "items": [],
  "page": 0,
  "page_size": 0,
  "total_pages": 0,
  "total_count": 0,
  "has_previous_page": true,
  "has_next_page": false
}
```

**Error Responses:**
- `400` Bad Request
- `401` Unauthorized
- `403` Forbidden
- `404` Not Found
- `500` Internal Server Error

---

#### 4. Cancel Payment By ID

**DELETE** `/epos/v1/payments/{id}`

Cancel a payment that has not yet been completed.

**Path Parameters:**
- `id` (required): string - Payment ID

**Headers:**
- `X-Session` (required): string
- `X-Partner-Id` (required): string
- `X-Application-Id` (required): string

**Request Body:**

```json
{
  "id": "string"
}
```

**Response (202 - Accepted):**

```json
{
  "id": "string"
}
```

**Error Responses:**
- `400` Bad Request
- `401` Unauthorized
- `403` Forbidden
- `404` Not Found
- `500` Internal Server Error

---

#### 5. Void Payment

**POST** `/epos/v1/payments/{payment_id}/reversal`

Void a previously completed payment. The funds will be returned to the cardholder.

**Path Parameters:**
- `payment_id` (required): string - Payment ID

**Headers:**
- `X-Session` (required): string
- `X-Partner-Id` (required): string
- `X-Application-Id` (required): string

**Request Body:**

```json
{
  "description": "Customer requested reversal due to duplicate transaction",
  "terminal_id": "80123457",
  "app_name": "MyPOSApp",
  "app_version": "1.0.0"
}
```

**Response (200 - OK):**

```json
{
  "payment_id": "67f82b071b62d43a1f65833b",
  "status": "InProgress",
  "amount": {
    "value": 1500,
    "currency_code": "EUR",
    "tip": 500
  },
  "created_at": "2025-10-27T07:56:24",
  "updated_at": "2025-10-27T07:56:24",
  "reference_number": "REF1234567890",
  "description": "Test request",
  "expire_at": "2025-11-27T07:56:24"
}
```

**Error Responses:**
- `400` Bad Request
- `401` Unauthorized
- `403` Forbidden
- `404` Not Found
- `500` Internal Server Error

---

#### 6. Create Refund

**POST** `/epos/v1/payments/refund`

Initiate a refund operation on a POS device.

**Headers:**
- `X-Session` (required): string
- `X-Partner-Id` (required): string
- `X-Application-Id` (required): string

**Request Body:**

```json
{
  "amount": {
    "value": 1500,
    "currency_code": "EUR"
  },
  "description": "Refund issued due to customer complaint",
  "terminal_id": "80123457",
  "app_name": "RefundApp",
  "app_version": "1.0.0"
}
```

**Response (200 - OK):**

```json
{
  "payment_id": "string",
  "status": "InProgress",
  "amount": {
    "value": 1500,
    "currency_code": "EUR"
  },
  "created_at": "2025-04-10T23:35:24.4054534+03:00",
  "updated_at": "0001-01-01T00:00:00",
  "reference_number": "string",
  "description": "string",
  "expire_at": "2025-05-10T23:35:24"
}
```

**Error Responses:**
- `400` Bad Request
- `401` Unauthorized
- `403` Forbidden
- `404` Not Found
- `500` Internal Server Error

---

### Terminal Operations

#### 7. Get Terminals

**GET** `/pos/v1/terminals`

Get a list of all POS devices associated with the merchant's account.

**Query Parameters:**
- `page`: integer - The page with transactions to be returned. Default is 1
- `size`: integer - The number of returned transactions. Possible values are 1÷100. Default value is 20
- `terminal_id`: string - The unique identifier of the POS device
- `serial_number`: string - The serial number of the POS device
- `model`: string - The model of the POS device

**Headers:**
- `X-Session` (required): string
- `X-Partner-Id` (required): string
- `X-Application-Id` (required): string

**Response (200 - OK):**

```json
{
  "pagination": {
    "page": 1,
    "page_size": 20,
    "total_pages": 1,
    "total_count": 5
  },
  "terminals": []
}
```

**Error Responses:**
- `400` Bad Request
- `401` Unauthorized
- `403` Forbidden
- `404` Not Found
- `500` Internal Server Error

---

#### 8. Get Terminal Details

**GET** `/pos/v1/terminals/{terminal_id}`

Get detailed information about the specified POS device.

**Path Parameters:**
- `terminal_id` (required): string - Terminal identifier

**Headers:**
- `X-Session` (required): string
- `X-Partner-Id` (required): string
- `X-Application-Id` (required): string

**Response (200 - OK):**

```json
{
  "terminal_name": "string",
  "outlet_id": 20577,
  "outlet_name": "string",
  "device_currency": "EUR",
  "status": "Active",
  "last_transaction_date": "2025-10-27",
  "transactions_count": 11,
  "settlement_account_number": "string",
  "settlement_account_name": "string",
  "settlement_account_currency": "EUR",
  "billing_descriptor": "string",
  "receipt_footer_row_1": "string",
  "receipt_footer_row_2": "string",
  "forbidden_preauthorization": false,
  "forbidden_moto": false,
  "forbidden_reversal": false,
  "forbidden_refund": false,
  "forbidden_topup": false,
  "card_topup_enabled": false,
  "terminal_id": "string",
  "serial_number": "string",
  "model": "string"
}
```

**Error Responses:**
- `400` Bad Request
- `401` Unauthorized
- `403` Forbidden
- `404` Not Found
- `500` Internal Server Error

---

#### 9. Get All Transactions

**GET** `/pos/v1/terminals/transactions`

Get all transactions across all terminals with filtering options.

**Query Parameters:**
- `page`: integer - The page with transactions to be returned. Default is 1
- `size`: integer - The number of returned transactions. Possible values are 1÷100. Default value is 20
- `terminal_id`: string - The unique identifier of the POS device
- `terminal_name`: string - The custom name of the POS device
- `date_from`: string - Starting date of the transaction list in format "YYYY-MM-DD"
- `date_to`: string - End date of the transaction list, default is "today" if not given. Format is "YYYY-MM-DD"
- `amount_from`: number - The minimum amount of a transaction
- `amount_to`: number - The maximum amount of a transaction

**Headers:**
- `X-Session` (required): string
- `X-Partner-Id` (required): string
- `X-Application-Id` (required): string

**Response (200 - OK):**

```json
{
  "pagination": {
    "page": 1,
    "page_size": 20,
    "total_pages": 1,
    "total_count": 50
  },
  "transactions": []
}
```

**Error Responses:**
- `400` Bad Request
- `401` Unauthorized
- `403` Forbidden
- `404` Not Found
- `500` Internal Server Error

---

#### 10. Get Terminal Transactions by ID

**GET** `/pos/v1/terminals/{terminal_id}/transactions`

List transactions made on a specific POS device.

**Path Parameters:**
- `terminal_id` (required): string - Terminal identifier

**Query Parameters:**
- `page`: integer - The page with transactions to be returned. Default is 1
- `size`: integer - The number of returned transactions. Possible values are 1÷100. Default value is 20
- `date_from`: string - Starting date of the transaction list in format "YYYY-MM-DD"
- `date_to`: string - End date of the transaction list, default is "today" if not given. Format is "YYYY-MM-DD"
- `stan`: string - System Trace Audit Number
- `amount_from`: number - The minimum amount of a transaction
- `amount_to`: number - The maximum amount of a transaction
- `reference_number`: string - The reference number of a transaction. Can be filtered by custom client reference

**Headers:**
- `X-Session` (required): string
- `X-Partner-Id` (required): string
- `X-Application-Id` (required): string

**Response (200 - OK):**

```json
{
  "transactions": []
}
```

**Error Responses:**
- `400` Bad Request
- `401` Unauthorized
- `403` Forbidden
- `404` Not Found
- `500` Internal Server Error

---

#### 11. Get Terminal Models

**GET** `/pos/v1/terminals/models`

Get available terminal models.

**Headers:**
- `X-Session` (required): string
- `X-Partner-Id` (required): string
- `X-Application-Id` (required): string

**Response (200 - OK):**

```json
{
  "code": "string",
  "description": "string"
}
```

**Error Responses:**
- `400` Bad Request
- `401` Unauthorized
- `403` Forbidden
- `404` Not Found
- `500` Internal Server Error

---

#### 12. Get Terminal Outlets

**GET** `/pos/v1/terminals/outlets`

Get terminal outlets information.

**Headers:**
- `X-Session` (required): string
- `X-Partner-Id` (required): string
- `X-Application-Id` (required): string

**Response (200 - OK):**

```json
{
  "outlet_id": 20577,
  "name": "string"
}
```

**Error Responses:**
- `400` Bad Request
- `401` Unauthorized
- `403` Forbidden
- `404` Not Found
- `500` Internal Server Error

---

#### 13. Terminal Activation Code

**POST** `/pos/v1/terminals/activation`

Generate activation code for a terminal.

**Note:** The related merchant (from `X-Session`) must have permission for Device Management.

**Headers:**
- `X-Session` (required): string
- `X-Partner-Id` (required): string
- `X-Application-Id` (required): string

**Response (200 - OK):**

```json
{
  "code": "string"
}
```

**Error Responses:**
- `400` Bad Request
- `401` Unauthorized
- `403` Forbidden
- `404` Not Found
- `500` Internal Server Error

---

#### 14. Terminal Deactivation

**POST** `/pos/v1/terminals/deactivation`

Generate deactivation code for a terminal.

**Headers:**
- `X-Session` (required): string
- `X-Partner-Id` (required): string
- `X-Application-Id` (required): string

**Response (200 - OK):**

```json
{
  "code": "string"
}
```

**Error Responses:**
- `400` Bad Request
- `401` Unauthorized
- `403` Forbidden
- `404` Not Found
- `500` Internal Server Error

---

#### 15. Get Terminal Receipt By Payment Reference

**GET** `/pos/v1/terminals/receipt/{payment_reference}`

Get receipt details by payment reference.

**Path Parameters:**
- `payment_reference` (required): string - Payment reference number

**Headers:**
- `X-Session` (required): string
- `X-Partner-Id` (required): string
- `X-Application-Id` (required): string

**Response (200 - OK):**

```json
{
  "is_declined": 0,
  "receipt_layout_version": 0,
  "exchange_rate": "string",
  "date": "string",
  "time": "string",
  "stan": "string",
  "terminal_id": "string",
  "merchant_id": "string",
  "merchant_name": "string",
  "address_line_1": "string",
  "address_line_2": "string",
  "pl_card_balance": "string",
  "pl_card_balance_currency": "string",
  "resp_code": "string",
  "reference_number": "string",
  "application_preferred_name": "string",
  "installment_type": "string",
  "installment_number": "string",
  "installment_interest_rate": "string",
  "installment_first_amount": "string",
  "installment_subseq_amount": "string",
  "installment_annual_perc_rate": "string",
  "installment_fee_rate": "string",
  "installment_total_amount": "string",
  "transaction_preauth_code": "string",
  "card_scheme": "string",
  "pan": "string",
  "emboss_name": "string",
  "amount": "string",
  "currency": "string",
  "auth_code": "string",
  "rrn": "string",
  "aid": "string",
  "amount_tip": "string",
  "amount_total": "string",
  "operator_code": "string",
  "dcc_amount": "string",
  "dcc_currency": "string",
  "tran_type": "string",
  "sign_row_1": "string",
  "sign_row_2": "string",
  "sign_row_3": "string",
  "exchange_rate_translation": "string",
  "tran_status": "string",
  "receipt_footer_row_1": "string",
  "receipt_footer_row_2": "string"
}
```

**Error Responses:**
- `400` Bad Request
- `401` Unauthorized
- `403` Forbidden
- `404` Not Found
- `500` Internal Server Error

---

#### 16. Terminal Refund

**POST** `/pos/v1/terminals/{terminal_id}/refund`

Process a refund on a specific terminal.

**Path Parameters:**
- `terminal_id` (required): string - Terminal identifier

**Headers:**
- `X-Session` (required): string
- `X-Partner-Id` (required): string
- `X-Application-Id` (required): string

**Request Body:**

```json
{
  "reference_number": "REF1234567890",
  "amount": {
    "value": 1500,
    "currency_code": "EUR"
  }
}
```

**Response (204 - No Content)**

**Error Responses:**
- `400` Bad Request
- `401` Unauthorized
- `403` Forbidden
- `404` Not Found
- `500` Internal Server Error

---

#### 17. Generate QR Code (Internal)

**POST** `/pos/v1/terminals/generate-qr`

Generate QR code for terminal activation.

**Note:** This is an internal endpoint.

**Request Body:**

```json
{
  "model": "N96",
  "sn": "string"
}
```

**Response (200 - OK):**

```json
{
  "code": "FCZ9",
  "activation_link": "myposactivation://FCZ9",
  "validity": "2025-11-27T09:56:24"
}
```

**Error Responses:**
- `400` Bad Request
- `401` Unauthorized
- `404` Not Found
- `409` Conflict
- `500` Internal Server Error

---

## Data Models

### Amount Object

```json
{
  "value": 1500,
  "currency_code": "EUR",
  "tip": 500
}
```

**Fields:**
- `value`: integer - Amount value (in smallest currency unit, e.g., cents)
- `currency_code`: string - Currency code (e.g., "EUR", "USD")
- `tip`: integer - Tip amount (optional)

### Payment Request

```json
{
  "reference_number": "string",
  "amount": {
    "value": 1500,
    "currency_code": "EUR",
    "tip": 500
  },
  "description": "string",
  "terminal_id": "string",
  "app_name": "string",
  "app_version": "string",
  "operator_code": "string"
}
```

### Payment Response

```json
{
  "payment_id": "string",
  "status": "string",
  "amount": {},
  "created_at": "string",
  "updated_at": "string",
  "reference_number": "string",
  "description": "string",
  "expire_at": "string"
}
```

### Terminal Transaction

```json
{
  "terminal_id": "string",
  "date": "string",
  "terminal_name": "string",
  "outlet_name": "string",
  "tran_status": "string",
  "payment_status": "string",
  "amount": 0.22,
  "currency": "EUR",
  "rrn": "string",
  "stan": "string",
  "settlement_date": "string",
  "settlement_amount": 0.22,
  "settlement_currency": "EUR",
  "fee": 0.01,
  "pan": "****1234",
  "card_scheme": "VISA",
  "payment_reference": "string",
  "billing_descriptor": "string",
  "reference_number": "string"
}
```

### Error Response (Problem Details)

```json
{
  "type": "string",
  "title": "string",
  "status": 400,
  "detail": "string",
  "instance": "string",
  "additionalProp1": "string",
  "additionalProp2": "string",
  "additionalProp3": "string"
}
```

### Validation Error Response

```json
{
  "type": "string",
  "title": "string",
  "status": 400,
  "detail": "string",
  "instance": "string",
  "errors": {
    "field1": ["error message 1", "error message 2"],
    "field2": ["error message"]
  }
}
```

---

## Common Response Codes

- **200 OK**: Request succeeded
- **201 Created**: Resource created successfully
- **202 Accepted**: Request accepted for processing
- **204 No Content**: Request succeeded with no response body
- **400 Bad Request**: Invalid request parameters or body
- **401 Unauthorized**: Missing or invalid authentication
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **409 Conflict**: Request conflicts with current state
- **500 Internal Server Error**: Server error occurred

---

## Notes

- All timestamps are in ISO 8601 format
- Amounts are typically represented in the smallest currency unit (e.g., cents for EUR/USD)
- Payment statuses include: `InProgress`, `Completed`, `Failed`, `Cancelled`
- Terminal statuses include: `Active`, `Inactive`, etc.
- Default pagination: page=1, size=20, max size=100
