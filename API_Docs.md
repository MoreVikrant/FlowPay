# FlowPay API Documentation

> Back to the [project README](README.md). Monetary amounts are integers in **paise** (for example, `500000` represents ₹5,000.00).

**Base URL (local):** `http://localhost:8080`

Most endpoints require the merchant API key in the `X-Api-Key` header. The registration endpoint is public and returns the API key and webhook secret. Payment, refund and settlement creation additionally require an `Idempotency-Key` header.

## Authentication headers

```http
X-Api-Key: <merchant_api_key>
Idempotency-Key: <unique-key-for-this-operation>
Content-Type: application/json
```

Header casing is not significant in HTTP. The backend source uses `X-API-Key` in the merchant controller and `X-Api-Key` in other controllers.

## Endpoint summary

| Domain | Method | Endpoint | Authentication | Purpose |
|---|---|---|---|---|
| Merchant | `POST` | `/api/merchants` | Public | Register a merchant |
| Merchant | `GET` | `/api/merchants/me` | API key | Get authenticated merchant |
| Merchant | `PUT` | `/api/merchants/me` | API key | Update merchant profile and webhook URL |
| Orders | `POST` | `/api/orders/create` | API key | Create an order |
| Orders | `GET` | `/api/orders` | API key | List merchant orders |
| Orders | `GET` | `/api/orders/{id}` | API key | Get one merchant order |
| Payments | `POST` | `/api/payments/create` | API key + idempotency | Create a payment attempt |
| Payments | `GET` | `/api/payments/{id}` | API key | Get payment details |
| Payments | `POST` | `/api/payments/{id}/process` | API key | Process payment through mock bank |
| Payments | `POST` | `/api/payments/{id}/check` | API key | Reconcile/check a pending payment |
| Refunds | `POST` | `/api/refunds/create` | API key + idempotency | Create full or partial refund |
| Refunds | `GET` | `/api/refunds` | API key | List merchant refunds |
| Settlements | `POST` | `/api/settlements/create` | API key + idempotency | Settle available merchant balance |
| Settlements | `GET` | `/api/settlements` | API key | List merchant settlements |
| Transactions | `GET` | `/api/transactions/list` | API key | List/filter payment attempts |
| Transactions | `GET` | `/api/transactions/{id}` | API key | Get payment details |
| Ledger | `GET` | `/api/transactions/ledger` | API key | List merchant ledger entries |
| Webhooks | `GET` | `/api/webhooks` | API key | List webhook delivery records |
| Webhooks | `POST` | `/api/webhooks/{id}/retry` | API key | Retry a webhook delivery |
| Health | `GET` | `/actuator/health` | Public in default config | Spring Boot health endpoint |

## Merchant APIs

### Register merchant

`POST /api/merchants`

No API key is required.

```bash
curl -X POST http://localhost:8080/api/merchants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Acme Store",
    "email": "payments@example.com"
  }'
```

Request fields:

| Field | Type | Validation |
|---|---|---|
| `name` | string | Required, maximum 100 characters |
| `email` | string | Required, valid email, maximum 100 characters |

The response contains a `merchant` object, `apiKey` and `webhookSecret`. Save both secrets when registering; the API key is used for subsequent API requests and the webhook secret is used to verify webhook signatures. Treat the response as sensitive.

### Get current merchant

`GET /api/merchants/me`

```bash
curl http://localhost:8080/api/merchants/me \
  -H "X-Api-Key: YOUR_API_KEY"
```

### Update merchant and configure webhook

`PUT /api/merchants/me`

```bash
curl -X PUT http://localhost:8080/api/merchants/me \
  -H "X-Api-Key: YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Acme Store",
    "email": "payments@example.com",
    "webhookUrl": "http://localhost:9000/webhooks/flowpay"
  }'
```

`name` and `email` are required. `webhookUrl` may be null/blank to leave no destination configured. Nonblank URLs must use HTTP or HTTPS and their host must be present in the backend `app.webhook-hosts` allowlist (`WEBHOOK_HOSTS`). User-info and URL fragments are rejected. The supplied local configuration defaults to `localhost,127.0.0.1,backend`; configure this deliberately for your environment.

## Order APIs

### Create order

`POST /api/orders/create`

```bash
curl -X POST http://localhost:8080/api/orders/create \
  -H "X-Api-Key: YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500000,
    "receipt": "course-order-001"
  }'
```

| Field | Type | Validation |
|---|---|---|
| `amount` | integer | Minimum 100 paise, maximum 10,000,000 paise |
| `receipt` | string | Required, maximum 100 characters |

The order currency defaults to `INR` and its initial status is `CREATED`. Creating an order persists an `order.created` event.

### List orders

`GET /api/orders`

### Get order

`GET /api/orders/{id}`

Both endpoints require `X-Api-Key` and return only records belonging to the authenticated merchant. A record belonging to another merchant is treated as not found.

## Payment APIs

### Create payment

`POST /api/payments/create`

Requires both `X-Api-Key` and `Idempotency-Key`.

```bash
curl -X POST http://localhost:8080/api/payments/create \
  -H "X-Api-Key: YOUR_API_KEY" \
  -H "Idempotency-Key: payment-course-0001" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORDER_ID_FROM_CREATE_ORDER",
    "method": "UPI",
    "scenario": "SUCCESS"
  }'
```

| Field | Allowed values / validation |
|---|---|
| `orderId` | Required order ID owned by the merchant |
| `method` | `CARD`, `UPI`, `NETBANKING`, `WALLET` |
| `scenario` | `SUCCESS`, `FAILURE`, `TIMEOUT`, `DELAYED_SUCCESS` |

The payment amount is taken from the existing order; the client does not submit a payment amount. The backend rejects an order that is already paid or has an active `SUCCESS`/`PENDING` payment. A repeated idempotency key with the same order, method and scenario returns the existing payment. Reusing the key with different values is rejected.

### Process payment

`POST /api/payments/{id}/process`

```bash
curl -X POST http://localhost:8080/api/payments/PAYMENT_ID/process \
  -H "X-Api-Key: YOUR_API_KEY"
```

A payment in `CREATED` state is sent to the mock bank simulator. Calling process again after the payment has left `CREATED` returns its current record without processing it again.

### Get payment / check pending payment

```bash
curl http://localhost:8080/api/payments/PAYMENT_ID \
  -H "X-Api-Key: YOUR_API_KEY"

curl -X POST http://localhost:8080/api/payments/PAYMENT_ID/check \
  -H "X-Api-Key: YOUR_API_KEY"
```

The check endpoint reconciles a payment only when its current status is `PENDING`. The scheduled reconciliation job also checks due delayed payments automatically.

## Refund APIs

### Create refund

`POST /api/refunds/create`

```bash
curl -X POST http://localhost:8080/api/refunds/create \
  -H "X-Api-Key: YOUR_API_KEY" \
  -H "Idempotency-Key: refund-course-0001" \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "SUCCESSFUL_PAYMENT_ID",
    "amount": 10000
  }'
```

| Field | Type | Validation |
|---|---|---|
| `paymentId` | string | Required successful payment owned by merchant |
| `amount` | integer | Minimum 1 paise, maximum 100,000,000 paise; must not exceed remaining refundable amount |

Refunds can be partial or full. A refund is recorded with status `SUCCESS` by the current simulator. The operation updates the payment's cumulative refunded amount, applies the fee reversal calculation and records a negative ledger adjustment. It emits a `refund.created` event.

### List refunds

`GET /api/refunds`

Returns the authenticated merchant's refunds, newest first.

## Settlement APIs

### Create settlement

`POST /api/settlements/create`

Requires `X-Api-Key` and `Idempotency-Key`. No request body is required.

```bash
curl -X POST http://localhost:8080/api/settlements/create \
  -H "X-Api-Key: YOUR_API_KEY" \
  -H "Idempotency-Key: settlement-course-0001"
```

The service creates a settlement for the merchant's full available balance. If the balance is zero or negative, the request is rejected. Repeating the same idempotency key returns the existing settlement.

### List settlements

`GET /api/settlements`

## Transaction and ledger APIs

### List transactions (payment attempts)

`GET /api/transactions/list`

Optional query parameters:

| Parameter | Meaning |
|---|---|
| `status` | Exact payment status filter |
| `from` | Inclusive start date, `YYYY-MM-DD` (UTC) |
| `to` | Inclusive end date, `YYYY-MM-DD` (UTC) |

```bash
curl "http://localhost:8080/api/transactions/list?status=SUCCESS&from=2026-10-01&to=2026-10-31" \
  -H "X-Api-Key: YOUR_API_KEY"
```

If both dates are supplied, `from` must not be after `to`. The `to` date includes the full UTC day. This endpoint returns payment records, not ledger rows.

### Get transaction details

`GET /api/transactions/{id}`

Returns the payment record for the given ID, scoped to the authenticated merchant.

### List ledger entries

`GET /api/transactions/ledger`

```bash
curl http://localhost:8080/api/transactions/ledger \
  -H "X-Api-Key: YOUR_API_KEY"
```

Ledger records include `referenceId`, `type`, signed `amount` and `balanceAfter`.

## Webhook APIs

### List webhook deliveries

`GET /api/webhooks`

Returns the authenticated merchant's webhook delivery history, including status, attempt count and last error.

### Retry webhook delivery

`POST /api/webhooks/{id}/retry`

```bash
curl -X POST http://localhost:8080/api/webhooks/WEBHOOK_RECORD_ID/retry \
  -H "X-Api-Key: YOUR_API_KEY"
```

A delivered webhook cannot be retried. The merchant must have a webhook URL configured. A retry resets the attempt counter and schedules the record for immediate delivery.

## Common response statuses and errors

The API returns JSON error bodies in the form:

```json
{
  "message": "A descriptive error message"
}
```

The shared exception handler maps business exceptions to their configured HTTP status, validation/malformed JSON/missing required header errors to `400`, and database uniqueness conflicts to `409`. Invalid or missing merchant API keys return `401`. Merchant-scoped lookups for records not owned by the caller return not found.

Typical cases:

| HTTP status | Meaning / example |
|---|---|
| `200` | Successful retrieval or operation |
| `201` | Merchant, order, payment or refund created |
| `400` | Invalid input, missing required header or malformed request |
| `401` | Missing or invalid API key |
| `404` | Record not found or belongs to another merchant |
| `409` | Duplicate/conflicting operation, insufficient balance or idempotency conflict |

The exact response status for each business error follows the exception raised by that service; use the returned `message` as the diagnostic detail.
