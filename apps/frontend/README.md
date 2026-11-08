# Citeria Frontend

Citeria is a booking platform: specialists publish services and working hours, clients search and book appointments. This repository is the Angular single-page application; it talks to a Spring Boot backend and uses JWT authentication with short-lived access tokens plus an HttpOnly refresh-token cookie.

## Tech Stack

- Angular 22 (standalone, zoneless, signals)
- TypeScript, RxJS
- Angular Material with custom SCSS design tokens (light and dark themes)
- Vitest

## Backend

- Local API: `http://localhost:8082/api`
- Swagger UI: `http://localhost:8082/swagger-ui.html`
- Endpoints and seed accounts: [docs/API.md](docs/API.md)

## Development

```bash
npm ci
npm start
```

Build and tests:

```bash
npm run build
npm test
```
