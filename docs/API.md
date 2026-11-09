# Citeria REST API

Booking platform API: specialists publish services and working hours, clients search and book appointments.

- **Base URL:** `http://localhost:8082`
- **Format:** JSON (`Content-Type: application/json`)
- **Interactive docs:** `http://localhost:8082/swagger-ui.html`

## Authentication

JWT access token (15 min) in the `Authorization` header, plus an httpOnly refresh-token cookie (7 days) scoped to `/api/auth`.

```
Authorization: Bearer <accessToken>
```

All endpoints require a valid access token **except** the authentication endpoints (`POST /api/auth/register`, `/login`, `/google`, `/refresh`, `/logout`) and public reads: `GET /api/offerings`, `/api/offerings/{id}`, `/api/offerings/{id}/slots`, `/api/specialist-detail/{id}` and `/api/users/{id}/avatar`.

- `register` / `login` return `accessToken` + set the `refresh_token` cookie.
- When the access token expires, call `POST /api/auth/refresh` (sends the cookie) to get a new one.
- `login` / `register` are rate-limited to **10 requests/min per IP** (`429 Too Many Requests` otherwise).

### User roles & types
- **role:** `USER` | `ADMIN` (set internally; `ADMIN` only via DB).
- **type:** `CLIENT` | `SPECIALIST` (chosen at registration, **immutable** afterwards). Specialists own services and working hours and get booked; clients book.

## Conventions

- **Dates:** appointment times are `Instant` (ISO-8601 UTC, e.g. `2026-09-01T10:00:00Z`); slot `from`/`to` are `LocalDate` (`2026-09-01`); working hours `startTime`/`endTime` are `LocalTime` (`09:00`).
- **Errors** use RFC-7807 `ProblemDetail`:

```json
{
  "status": 400,
  "title": "Validation Failed",
  "detail": "Request contains invalid fields.",
  "code": "VALIDATION_ERROR",
  "timestamp": "2026-09-01T10:00:00Z",
  "errors": { "startTime": "Start time is required" }
}
```

Common codes: `VALIDATION_ERROR` (400), `INVALID_REQUEST_BODY` (400), `AUTHENTICATION_FAILED`/`UNAUTHORIZED` (401), `FORBIDDEN` (403), `RESOURCE_NOT_FOUND` (404), `SLOT_ALREADY_BOOKED`/`CONFLICT` (409), `RATE_LIMITED` (429).

### Seed accounts (dev profile)
Password for all: **`Password1!`** — `admin@citeria.test` (ADMIN), `anna@citeria.test` / `boris@citeria.test` (SPECIALIST), `clara@citeria.test` / `dan@citeria.test` (CLIENT).

---

## Pagination & sorting

Every list endpoint (`GET /api/users`, `/api/offerings`, `/api/working-hours`, `/api/appointments`) is paginated and accepts the same query parameters.

| Param | Type | Default | Description |
|---|---|---|---|
| `page` | int | `0` | Page index, **0-based** |
| `size` | int | `20` | Items per page, at most `50` |
| `sort` | string | per endpoint | `property,direction` — e.g. `sort=startTime,desc`. Repeat the param for multi-field sorting (`?sort=status,asc&sort=startTime,desc`); `direction` is optional and defaults to `asc`. |

**Sort fields are whitelisted.** Any property not in the allowed list below is silently ignored (it is *not* an error); if nothing valid remains, the endpoint's default sort is applied. This prevents sorting by arbitrary/internal columns.

| Endpoint | Allowed `sort` fields | Default sort |
|---|---|---|
| `GET /api/users` | `createdAt`, `lastName`, `email` | `createdAt,desc` |
| `GET /api/offerings` | `name`, `priceAmount`, `createdAt` | `name,asc` |
| `GET /api/working-hours` | `createdAt` | `createdAt,desc` |
| `GET /api/appointments` | `startTime`, `status`, `createdAt` | `startTime,asc` |

Filters (documented per endpoint) combine with pagination — all filters are AND-ed, and omitted filters are ignored.

**Request**
```http
GET /api/appointments?status=PENDING&page=0&size=10&sort=startTime,desc
Authorization: Bearer <token>
```

**Response envelope** — the payload is always wrapped:
```json
{
  "content": [ { "id": "…" } ],
  "page": 0,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

| Field | Description |
|---|---|
| `content` | Array of items for the current page |
| `page` | Current page index (0-based, echoes the request) |
| `size` | Page size used |
| `totalElements` | Total matching items across all pages |
| `totalPages` | Total number of pages |
| `first` / `last` | Whether this is the first / last page |

Requesting a page beyond the end returns `200` with an empty `content` array (not a 404).

---

## Auth — `/api/auth`

| Method | Path | Description |
|---|---|---|
| POST | `/register` | Create an account; returns token + sets refresh cookie |
| POST | `/login` | Authenticate; returns token + sets refresh cookie |
| POST | `/google` | Sign in with a Google ID token; returns token + sets refresh cookie |
| POST | `/refresh` | Issue a new access token from the refresh cookie |
| POST | `/logout` | Revoke the refresh token and clear the cookie |

**Register**
```http
POST /api/auth/register
Content-Type: application/json

{
  "firstName": "Clara",
  "lastName": "Client",
  "email": "clara@example.com",
  "phone": "+3725550101",
  "password": "Password1!",
  "type": "CLIENT"
}
```
`phone` is optional.
Response `201`:
```json
{
  "accessToken": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "user": { "id": "…", "firstName": "Clara", "email": "clara@example.com", "role": "USER", "type": "CLIENT", "isActive": true, "createdAt": "…", "avatarVersion": null, "hasPassword": true }
}
```

**Login**
```http
POST /api/auth/login
Content-Type: application/json

{ "email": "anna@citeria.test", "password": "Password1!" }
```

**Google sign-in**
```http
POST /api/auth/google
Content-Type: application/json

{ "credential": "<Google ID token>", "type": "SPECIALIST" }
```
`credential` is the ID token from Google Identity Services. The backend verifies its signature, issuer,
audience (`GOOGLE_CLIENT_ID`) and that the email is verified. The user is matched by Google account
first, then by email (the Google account gets linked to the existing user); otherwise a new user is
created with `type` from the request (`CLIENT` by default) and no password. Response is the same as
login. Errors: `401` for an invalid token, an unverified email, a deactivated user or an email already
linked to another Google account; `503 FEATURE_UNAVAILABLE` when `GOOGLE_CLIENT_ID` is not set.

**Refresh** — `POST /api/auth/refresh` (no body; refresh cookie sent automatically). Returns `{ "accessToken": "…", "tokenType": "Bearer" }`.

**Logout** — `POST /api/auth/logout` → `204`.

---

## Users — `/api/users`

| Method | Path | Description | Access |
|---|---|---|---|
| GET | `/` | List users (filters: `role`, `type`, `active`, `search`) | ADMIN |
| GET | `/me` | Current authenticated user | any |
| GET | `/{id}` | Get user by id | self or ADMIN |
| PATCH | `/{id}` | Update profile (firstName, lastName, email, phone) | self or ADMIN |
| PATCH | `/{id}/password` | Change password | self only |
| DELETE | `/{id}` | Deactivate (soft) | self or ADMIN |
| DELETE | `/{id}/hard` | Hard delete | ADMIN |
| PATCH | `/{id}/restore` | Reactivate | ADMIN |

```http
PATCH /api/users/{id}
Authorization: Bearer <token>

{ "firstName": "Clara", "phone": "+3725550102" }
```
Send `"phone": ""` to remove the phone number.

```http
PATCH /api/users/{id}/password
Authorization: Bearer <token>

{ "currentPassword": "Password1!", "newPassword": "NewPass2@" }
```
For a user without a password (signed up with Google, `hasPassword: false`) `currentPassword` may be
omitted: the request sets the first password.

Every `UserResponseDto` includes an **`avatarVersion`** number (`null` when the user has no avatar) used to build the avatar URL (see below), and a **`hasPassword`** boolean (`false` for users who signed up with Google and have not set a password yet).

---

## Avatars — `/api/users/{id}/avatar`

One avatar per user, stored as binary in the database. Uploads are center-cropped to 512×512 and re-encoded as JPEG (EXIF metadata is dropped, transparency becomes white). The image is served as **raw bytes**, not JSON.

| Method | Path | Description | Access |
|---|---|---|---|
| POST | `/api/users/{id}/avatar` | Upload / replace avatar (`multipart/form-data`) | self or ADMIN |
| GET | `/api/users/{id}/avatar` | Download avatar image | public |
| DELETE | `/api/users/{id}/avatar` | Remove avatar | self or ADMIN |

**Constraints**
- Allowed types: `image/png`, `image/jpeg` (`app.avatar.allowedContentTypes`), detected from the file content; the `Content-Type` of the part is ignored.
- Max source dimensions: **40 megapixels** (`app.avatar.maxSourcePixels`).
- Max size: **5 MB** (`app.avatar.maxSizeBytes`; servlet limits `max-file-size=5MB`, `max-request-size=6MB`).
- Uploading again **replaces** the existing avatar.

**Upload** — form field name must be `file`:
```http
POST /api/users/44444444-4444-4444-4444-444444444444/avatar
Authorization: Bearer <token>
Content-Type: multipart/form-data; boundary=----X

------X
Content-Disposition: form-data; name="file"; filename="me.png"
Content-Type: image/png

<binary png bytes>
------X--
```
Response `204 No Content`.

```bash
curl -X POST http://localhost:8082/api/users/{id}/avatar \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@me.png;type=image/png"
```

**Download**
```http
GET /api/users/44444444-4444-4444-4444-444444444444/avatar?v=<avatarVersion>
```
Response `200` — image bytes with:
- `Content-Type: image/jpeg` (avatars uploaded before re-encoding may be `image/png`)
- `Cache-Control: max-age=31536000, public, immutable` when `v` equals the current `avatarVersion`, otherwise `no-cache`
- `Last-Modified: <upload time>`

**Delete** — `DELETE /api/users/{id}/avatar` → `204 No Content`.

**Errors**
| Status | When |
|---|---|
| `400` `VALIDATION_ERROR` | Empty file, larger than 5 MB, not a valid PNG/JPEG, or too many pixels (`errors.file`) |
| `403` `FORBIDDEN` | Uploading/deleting someone else's avatar without ADMIN |
| `404` `RESOURCE_NOT_FOUND` | User does not exist, or the user has no avatar |

> **Frontend note:** the endpoint is public, so a plain `<img src="…/users/{id}/avatar?v={avatarVersion}">` works. Skip the image when `avatarVersion` is `null`. Offering responses expose the same value as `specialistAvatarVersion`.

---

## Specialist profile — `/api/specialist-detail`

| Method | Path | Description |
|---|---|---|
| GET | `/{id}` | Public profile of a specialist: active services + working hours |

```http
GET /api/specialist-detail/22222222-2222-2222-2222-222222222222
```
Response `200`:
```json
{
  "id": "2222...", "firstName": "Anna", "lastName": "Specialist", "isActive": true,
  "offerings": [ { "id": "aaaa...1", "name": "Consultation", "durationMinutes": 60, "priceAmount": 60.00, "currency": "EUR", "isActive": true } ],
  "workingHours": [ { "id": "…", "dayOfWeek": "MONDAY", "startTime": "09:00", "endTime": "17:00", "isActive": true } ]
}
```

---

## Offerings — `/api/offerings`

An offering belongs to a specialist (the creator). Guests can read active offerings from active specialists, specialist profiles and available slots. Inactive offerings are visible only to their owner or an administrator; an unauthorized `active=false` filter is treated as `active=true`. Writing requires authentication and specialist ownership.

| Method | Path | Description | Access |
|---|---|---|---|
| GET | `/` | List/search offerings (filters: `search`, `specialistId`, `active`, `minPrice`, `maxPrice`) | public; inactive: owner or ADMIN |
| GET | `/{id}` | Get offering by id | public; inactive: owner or ADMIN |
| GET | `/{id}/slots` | Available booking slots (`from`, `to` dates) | public |
| POST | `/` | Create an offering | SPECIALIST |
| PATCH | `/{id}` | Update an offering | owner or ADMIN |
| DELETE | `/{id}` | Deactivate (soft) | owner or ADMIN |
| DELETE | `/{id}/hard` | Hard delete | owner or ADMIN |
| PATCH | `/{id}/restore` | Reactivate | owner or ADMIN |

**Create** (specialist taken from the token):
```http
POST /api/offerings
Authorization: Bearer <specialist token>

{
  "name": "Consultation",
  "description": "Initial consultation",
  "durationMinutes": 60,
  "priceAmount": 60.00,
  "currency": "EUR"
}
```
`durationMinutes` 15–480; `currency` is a 3-letter code.

**Available slots** — computed on the fly from the specialist's working hours minus taken slots; only from the **next day** onward.
```http
GET /api/offerings/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1/slots?from=2026-09-01&to=2026-09-03
Authorization: Bearer <token>
```
Response `200`:
```json
[
  { "startTime": "2026-09-01T09:00:00Z", "endTime": "2026-09-01T10:00:00Z" },
  { "startTime": "2026-09-01T10:00:00Z", "endTime": "2026-09-01T11:00:00Z" }
]
```

---

## Working hours — `/api/working-hours`

Weekly schedule of a specialist; one window per weekday. No active hours ⇒ no bookable slots.

| Method | Path | Description | Access |
|---|---|---|---|
| GET | `/` | List (filters: `specialistId`, `dayOfWeek`, `active`) | any |
| GET | `/{id}` | Get by id | any |
| POST | `/` | Add a working-hours window | SPECIALIST |
| PATCH | `/{id}` | Update window (startTime, endTime, isActive) | owner or ADMIN |
| DELETE | `/{id}` | Delete window | owner or ADMIN |

```http
POST /api/working-hours
Authorization: Bearer <specialist token>

{ "dayOfWeek": "MONDAY", "startTime": "09:00", "endTime": "17:00" }
```
`dayOfWeek`: `MONDAY`…`SUNDAY`. Toggle availability with `PATCH … { "isActive": false }`.

---

## Appointments — `/api/appointments`

### Lifecycle
```
PENDING ──accept──▶ AWAITING_PAYMENT ──pay──▶ CONFIRMED ──complete──▶ COMPLETED
   │                      │                       │
 reject/cancel        cancel/expire            cancel
   ▼                      ▼                       ▼
REJECTED              EXPIRED/CANCELLED        CANCELLED
```
A request **does not hold the slot** until the specialist accepts it — several clients may request the same slot; on accept the rest are auto-rejected. The slot is occupied only in `AWAITING_PAYMENT` / `CONFIRMED` / `COMPLETED`. Unpaid windows and stale requests are released automatically.

| Method | Path | Description | Access |
|---|---|---|---|
| GET | `/` | List own appointments (filters: `status`, `from`, `to`, `offeringId`) | participant; ADMIN sees all |
| GET | `/{id}` | Get appointment | participant or ADMIN |
| POST | `/` | Book a slot → `PENDING` | CLIENT |
| POST | `/{id}/accept` | Accept request → `AWAITING_PAYMENT` | specialist |
| POST | `/{id}/reject` | Decline request → `REJECTED` | specialist |
| POST | `/{id}/pay` | Pay (mocked) → `CONFIRMED` | client |
| POST | `/{id}/cancel` | Cancel → `CANCELLED` | participant |
| POST | `/{id}/complete` | Mark done → `COMPLETED` | specialist |
| DELETE | `/{id}` | Hard delete | ADMIN |

**Book** — client sends only `offeringId` + `startTime` (must match an available slot); `endTime` is derived from the service duration.
```http
POST /api/appointments
Authorization: Bearer <client token>

{
  "offeringId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1",
  "startTime": "2026-09-01T10:00:00Z"
}
```
Response `201`:
```json
{
  "id": "…",
  "clientId": "…", "clientName": "Clara Client", "clientEmail": "clara@citeria.test",
  "offeringId": "aaaa...1", "offeringName": "Consultation",
  "specialistId": "2222...", "specialistName": "Anna Specialist",
  "startTime": "2026-09-01T10:00:00Z", "endTime": "2026-09-01T11:00:00Z",
  "status": "PENDING", "priceAmount": 60.00, "paymentDeadline": null
}
```

**Specialist sees pending requests:** `GET /api/appointments?status=PENDING`.

**Accept** (specialist): `POST /api/appointments/{id}/accept` → status `AWAITING_PAYMENT`, `paymentDeadline` set to `min(acceptedAt + 24h, start − 6h)`.

**Pay** (client, mocked — card data accepted but never validated/charged):
```http
POST /api/appointments/{id}/pay
Authorization: Bearer <client token>

{ "cardNumber": "4111111111111111", "cardHolder": "CLARA CLIENT", "expiry": "12/29", "cvc": "123" }
```
→ status `CONFIRMED`. The body is optional.

**Cancel** (`/cancel`) and **Complete** (`/complete`) take no body.
