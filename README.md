# novabank-api-gateway

Single entry point for the UI, Postman and Hoppscotch. Spring Cloud Gateway (WebFlux).

| | |
|---|---|
| Port | 8080 |
| Does | Routing, JWT validation at the edge, admin path protection, CORS, `X-Correlation-Id`, request timing logs |
| Extra endpoint | `GET /api/system/health` (ADMIN): probes all backends in parallel |

## Routes

| Path | Service |
|---|---|
| `/api/auth/**`, `/api/admin/users/**` | auth-service |
| `/api/accounts/**`, `/api/admin/accounts/**` | account-service |
| `/api/transactions/**`, `/api/admin/transactions/**` | transaction-service |
| `/internal/**` | not routed (denied) |

## Configuration

| Variable | Local default |
|---|---|
| `AUTH_SERVICE_URL` | `http://localhost:8081` |
| `ACCOUNT_SERVICE_URL` | `http://localhost:8082` |
| `TRANSACTION_SERVICE_URL` | `http://localhost:8083` |
| `JWT_SECRET` | same local value as the services |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` |
