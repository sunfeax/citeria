# Pre-deploy tasks

Everything left before Citeria can go live as a portfolio project. Tick items off as they are done.

Frontend domain: `https://citeria.sunfeax.com`.

## Decisions needed first

- [ ] Hosting for the backend, database and frontend.
- [ ] API domain. `environment.prod.ts` points to `api.citeria.com`, while the frontend lives on `citeria.sunfeax.com`.
- [ ] Demo data. Seed accounts only load with the `dev` profile, which must not run in production. Choose a separate demo seed or accounts created by hand.
- [ ] Public catalog. `GET /api/offerings` requires a login, so visitors see nothing until they sign in. Keep it or open it.
- [ ] Admin panel: in scope or not.

## Product: must have

### Avatars
- [ ] Upload, replace and delete in the profile page. Refresh the session user afterwards so `avatarVersion` changes.

### Booking flow
- [ ] Specialist page: profile, active offerings, working hours. `GET /specialist-detail/{id}`
- [ ] Slot picker for a date range. `GET /offerings/{id}/slots?from&to`
- [ ] Create a booking. `POST /appointments`
- [ ] Mock payment for `AWAITING_PAYMENT`, clearly labeled as a demo. `POST /appointments/{id}/pay`

### My bookings
- [ ] List with status and date filters and pagination. `GET /appointments?status&from&to`
- [ ] Role actions: client pays and cancels; specialist accepts, rejects and completes.
- [ ] Enable "My bookings" in the sidebar.

### Specialist dashboard
- [ ] Own offerings: list including inactive, create, edit, deactivate, restore.
- [ ] Working hours by weekday. `GET/POST/PATCH/DELETE /working-hours`

### Error handling
- [ ] Normalize `HttpErrorResponse` into one app error type.
- [ ] Global interceptor: 5xx, network errors and 403 show a toast.

## Product: dead ends to close

- [ ] Home page: `MainLayoutComponent` has no child route for `/`.
- [ ] "Forgot password" link on the login page: the backend has no endpoint. Hide it or build it.
- [ ] "Messages" and "Help" in the sidebar: no API behind them. Remove them.
- [ ] Catalog sorting by name, price and date (`sort=name,asc`).

## Backend hardening

- [ ] Run production without the `dev` profile. `compose.yaml` defaults to `dev`, which loads test accounts with known passwords.
- [ ] Set `app.jwt.refreshCookieSecure=true`.
- [ ] Set `app.cors.allowed-origin` to the production frontend origin.
- [ ] Rate limiter: `RateLimitFilter` reads `X-Forwarded-For`, which a client can forge to bypass the login limit. Trust it only from the reverse proxy.
- [ ] Remove `/h2-console/**` from the `permitAll` list. H2 is not used.
- [ ] Decide whether Swagger stays public in production.
- [ ] Remove the default datasource username from `application.properties`.
- [ ] Fix `EXPOSE 8080` in the backend Dockerfile. The app listens on `8082`.
- [ ] Set `APP_BOOKING_ZONE`. It defaults to `UTC`.

## Infrastructure

- [ ] Production compose or host config without Adminer. Today it is published on all interfaces at `8081`.
- [ ] Frontend production build and hosting. There is no Dockerfile or static host config yet, and it needs SPA fallback to `index.html`.
- [ ] Reverse proxy with HTTPS.
- [ ] Production secrets: `APP_JWT_SECRET`, database password, `GOOGLE_CLIENT_ID`.
- [ ] Scheduled Postgres backups with one tested restore. Avatars live in the database, so they are covered too.
- [ ] CI: build both apps on push. There is no pipeline yet.

## Google sign-in

- [ ] End-to-end check with the real Client ID.
- [ ] Add the production origin in Google Cloud Console.
- [ ] Move the OAuth app from Testing to Production.

## Launch

- [ ] Reduce the initial bundle: 620 kB against a 500 kB budget.
- [ ] Smoke test in production: register, log in, browse, book, pay, cancel, upload an avatar, sign in with Google.
- [ ] README with screenshots, stack and demo accounts.
