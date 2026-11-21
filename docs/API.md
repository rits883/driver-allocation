# LogiNext Driver Allocation — REST API Reference

**Version:** 1.0 (draft aligned to the discussed implementation)  
**Local base URL:** `http://localhost:8085`  
**Swagger UI:** `http://localhost:8085/swagger-ui/index.html`  
**OpenAPI JSON:** `http://localhost:8085/v3/api-docs`

> **Verification note:** Driver endpoints below are grounded in the controller/service code developed in our walkthrough. The precise allocation endpoint URLs and the complete `AllocationRequest`/`AllocationResponse` fields were not shared, so they are explicitly marked **VERIFY IN SOURCE** rather than invented.

## 1. Driver management

### 1.1 Register driver

**`POST /api/v1/drivers`**

```http
POST /api/v1/drivers HTTP/1.1
Content-Type: application/json

{"driverCode":"DRIVER-101"}
```

**201 Created** (illustrative response):

```json
{
  "id": 1,
  "driverCode": "DRIVER-101",
  "status": "ACTIVE",
  "availableAt": 0
}
```

**Possible errors:** `400` invalid input, `409` duplicate driver code. Database uniqueness must be handled to ensure `409` under concurrent registration.

### 1.2 List drivers

**`GET /api/v1/drivers`**

```bash
curl 'http://localhost:8085/api/v1/drivers?page=0&size=10&sort=id,asc'
```

When using Spring Data's `PagedModel` wrapper, a response has this structure:

```json
{
  "content": [
    {
      "id": 1,
      "driverCode": "DRIVER-101",
      "status": "ACTIVE",
      "availableAt": 0
    }
  ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

The example above assumes one stored driver. Page numbers are zero-based. Valid sort fields correspond to `DriverEntity` properties such as `id`, `driverCode`, `status`, `availableAt`, and `createdAt`. Validate sort inputs to avoid invalid-sort server errors.

**Alternative pagination contract:** If the project adopted explicit `page`, `size`, `sortBy`, `direction` parameters instead of `Pageable`, use that controller's actual query parameters. Do not publish both styles unless both are supported.

### 1.3 Get driver by ID

**`GET /api/v1/drivers/{id}`**

```bash
curl 'http://localhost:8085/api/v1/drivers/1'
```

**200 OK:** `DriverResponse` as shown above.  
**404 Not Found:** Unknown driver ID.

### 1.4 Activate/deactivate driver

**`PATCH /api/v1/drivers/{id}/status`**

```bash
curl -i -X PATCH 'http://localhost:8085/api/v1/drivers/1/status' \
  -H 'Content-Type: application/json' \
  -d '{"status":"INACTIVE"}'
```

**200 OK:**

```json
{
  "id": 1,
  "driverCode": "DRIVER-101",
  "status": "INACTIVE",
  "availableAt": 0
}
```

Reactivate with `{"status":"ACTIVE"}`. Only `ACTIVE` and `INACTIVE` are accepted. Status changes are coordinated with allocation through the same PostgreSQL singleton-row lock.

**Possible errors:** `400` invalid or missing status, `404` missing driver.

## 2. Allocation APIs — VERIFY IN SOURCE

The application log confirms a controller method named:

```java
AllocationController.simulate(AllocationRequest request)
```

It requires a request body. The exact HTTP method/path, full request fields, response fields, and whether simulation writes to the database **must be taken from the current `AllocationController`, `AllocationRequest`, and `AllocationResponse`**.

### 2.1 Simulation endpoint

| Property | Current documentation status |
|---|---|
| URL | **VERIFY IN SOURCE** |
| HTTP method | **VERIFY IN SOURCE** |
| Request type | `AllocationRequest` |
| Response type | `AllocationResponse` |
| Missing request body | `400 Bad Request` |
| Database side effects | **VERIFY IN SOURCE** |

Do not assume simulation is read-only based on its name.

### 2.2 Persistent allocation endpoint

| Property | Current documentation status |
|---|---|
| URL | **VERIFY IN SOURCE** |
| HTTP method | **VERIFY IN SOURCE** |
| Request/response DTOs | **VERIFY IN SOURCE** |
| Order reference | Used for idempotency in the agreed design |
| Driver selection | Lowest-index ACTIVE driver with `availableAt <= orderTime` |
| Availability update | `orderTime + travelTime` on successful assignment |
| Concurrency | Lock coordination row `id = 1` in transaction |
| No driver available | Business outcome `No Food :-(`; HTTP mapping **VERIFY IN SOURCE** |

### 2.3 Original challenge example (algorithm contract, not an asserted REST payload)

```text
N = 6, M = 2
Orders (O,T):
(1,10)
(4,20)
(15,5)
(22,20)
(24,10)
(25,10)

Expected:
C1 - D1
C2 - D2
C3 - D1
C4 - D1
C5 - D2
C6 - No Food :-(
```

The original problem statement does **not** prescribe a REST JSON schema. Populate exact JSON examples only after inspecting the implemented records.

## 3. Error response format

The proposed global exception handler uses Spring `ProblemDetail` with an additional `errorCode` property:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Malformed or missing request body",
  "instance": "/api/v1/drivers/1/status",
  "errorCode": "INVALID_REQUEST_BODY"
}
```

| HTTP status | Typical use |
|---|---|
| `200 OK` | Successful reads/status updates and potentially allocation responses |
| `201 Created` | Driver registration |
| `400 Bad Request` | Invalid JSON, invalid enum, failed validation |
| `404 Not Found` | Driver not found |
| `409 Conflict` | Duplicate driver code or allocation business conflict, depending on implementation |
| `500 Internal Server Error` | Unexpected failure; internal details not exposed |

Check actual exception handlers for final mappings; a catch-all handler should not convert framework `404` or `405` responses into `500`.

## 4. Local setup and usage

The observed local environment used PostgreSQL 17 on `localhost:5434`, database `driver_allocation`, and Spring Boot on port `8085`.

```bash
./mvnw spring-boot:run
```

Open Swagger UI at `http://localhost:8085/swagger-ui/index.html` and use the generated schema to confirm actual endpoint paths and required request bodies.

### Quick manual commands

```bash
# Register
curl -i -X POST 'http://localhost:8085/api/v1/drivers' \
  -H 'Content-Type: application/json' \
  -d '{"driverCode":"DRIVER-101"}'

# List
curl -i 'http://localhost:8085/api/v1/drivers?page=0&size=10&sort=id,asc'

# Retrieve
curl -i 'http://localhost:8085/api/v1/drivers/1'

# Deactivate
curl -i -X PATCH 'http://localhost:8085/api/v1/drivers/1/status' \
  -H 'Content-Type: application/json' \
  -d '{"status":"INACTIVE"}'
```

