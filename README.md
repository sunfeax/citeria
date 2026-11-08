# Citeria

Citeria is a booking platform: specialists publish services and working hours, clients search and book appointments.

## Structure

- `apps/frontend`: Angular single-page application
- `apps/backend`: Spring Boot REST API
- `docs/API.md`: API contract and seed accounts
- `docs/angular-roadmap.md`: project plan

## Running locally

Copy `.example.env` to `.env` and fill in the values, then start the database and the API:

```bash
docker compose up -d --build
```

The API is available at `http://localhost:8082`, Swagger UI at `http://localhost:8082/swagger-ui.html`, Adminer at `http://localhost:8081`. Postgres is published on `127.0.0.1:5433`.

Start the frontend in a second terminal:

```bash
cd apps/frontend
npm ci
npm start
```

The app is available at `http://localhost:4200`.
