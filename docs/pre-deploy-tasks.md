# Project plan

Ship the core first, then add one finished feature at a time to the live site. Tick items off as they are done.

Domain: `https://citeria.sunfeax.com`, with the API at `https://citeria.sunfeax.com/api`.

## Decisions

| Topic | Decision |
|---|---|
| Hosting | One VPS: Docker Compose, Postgres and a reverse proxy with automatic HTTPS |
| API domain | Same origin as the frontend, under `/api` |
| Demo data | A separate production demo seed with fictional specialists and demo accounts; the `dev` profile stays off |
| Public catalog | Guests browse active offerings; booking actions require login |
| Phone | Optional |
| Admin | No admin panel |
| Email registration | Signs the user in immediately, without a trip to the login page |
| Specialist onboarding | Optional. An empty state on My services suggests adding an offering and working hours; a specialist can do nothing |
| Account roles | One account, one role. Specialists do not book other specialists |
| Specialist profile | Optional "About" text |
| Payment | Demo payment in version 1, clearly labeled; Stripe sandbox after launch |
| Google registration | After launch: the Google button behaves the same on login and register, and nothing is saved until a prefilled form is submitted. Until then new Google users become clients |
| Password reset | After launch: inside the backend, by email; no separate service |
| Notifications | After launch: email for booking events; no email for chat messages |
| Chat | After launch: WebSocket; a conversation opens after the first booking request |
| Help | After launch: static FAQ with a support email link; no form |
| Swagger | Closed in production |

# Version 1

## 1. Booking

- [ ] Optional `About` field for specialists: database, API and profile settings.
- [ ] Public specialist page `/specialists/:id`: photo, name, About, offerings, working hours. Add it to the public routes, security rules and `isOnPublicPage`. Catalog `Book` opens this page.
- [ ] Booking panel on that page: offering, day strip, free times. `GET /offerings/{id}/slots?from&to`
- [ ] Create a booking; login is required at this step. `POST /appointments`
- [ ] Hide booking actions from specialists. The backend already limits booking to clients.

## 2. My bookings

- [ ] Client page with Upcoming and Past tabs, cards grouped by date, pagination. `GET /appointments?status&from&to`
- [ ] Card actions by status: pay with time left, waiting for confirmation, cancel.
- [ ] Demo payment, clearly labeled. `POST /appointments/{id}/pay`

## 3. Specialist pages

- [ ] Sidebar by role. Client: Find specialists, My bookings. Specialist: Bookings, My services.
- [ ] Bookings: incoming requests with accept and reject on top, then a real week board; a day list on phones. Specialist marks visits as completed.
- [ ] My services: list including inactive, create, edit, deactivate, restore. Empty state suggests the first offering and working hours.
- [ ] Working hours by weekday on the same page. `GET/POST/PATCH/DELETE /working-hours`

## 4. Finish the product

- [ ] Email registration signs the user in. The backend already returns a session; the frontend needs to start it. Clients go to the catalog or return path; specialists go to My services.
- [ ] Mobile navigation: the sidebar takes 280 px on phones. Put it behind a menu button.
- [ ] Remove dead ends until the features exist: "Forgot password", "Messages", "Help".
- [ ] Normalize `HttpErrorResponse` into one app error type.
- [ ] Global interceptor: 5xx, network errors and 403 show a toast.

## 5. Backend hardening

- [ ] Run production without the `dev` profile. `compose.yaml` defaults to `dev`, which loads test accounts with known passwords.
- [ ] Production demo seed, with a periodic reset so visitors do not pile up data.
- [ ] Set `app.jwt.refreshCookieSecure=true`.
- [ ] Set `app.cors.allowed-origin` for the production origin, or remove CORS there since the API is same-origin.
- [ ] Remove `/h2-console/**` from the `permitAll` list. H2 is not used.
- [ ] Close Swagger in production.
- [ ] Remove the default datasource username from `application.properties`.
- [ ] Fix `EXPOSE 8080` in the backend Dockerfile. The app listens on `8082`.
- [ ] Set `APP_BOOKING_ZONE`. It defaults to `UTC`.

## 6. Deploy

- [ ] Set `baseUrl` in `environment.prod.ts` to `/api`. It still points to `api.citeria.com`.
- [ ] Production compose on the VPS, without Adminer.
- [ ] Reverse proxy: HTTPS, `/api` to the backend, `X-Forwarded-For`, and static frontend with fallback to `index.html`.
- [ ] Production secrets: `APP_JWT_SECRET`, database password, `GOOGLE_CLIENT_ID`.
- [ ] Scheduled Postgres backups with one tested restore. Avatars live in the database, so they are covered too.
- [ ] CI: build and lint both apps on push.
- [ ] Google: end-to-end check with the real Client ID, production origin in Google Cloud Console, OAuth app moved from Testing to Production.
- [ ] Smoke test in production: register, log in, browse, book, pay, cancel, upload an avatar, sign in with Google.
- [ ] README with screenshots, stack and demo accounts.

# After launch

Each item ships on its own, in this order.

## 7. Stripe payments (sandbox)

- [ ] Stripe account with a sandbox and test API keys. No live mode and no real money.
- [ ] `POST /appointments/{id}/checkout`: create a Checkout Session for `AWAITING_PAYMENT`, with the amount taken from the server.
- [ ] Public webhook with signature verification: `checkout.session.completed` confirms the appointment and handles repeated events safely.
- [ ] Remove the mock `POST /appointments/{id}/pay`.
- [ ] Expire the Checkout Session when the payment window ends.
- [ ] Refund in the sandbox when a paid appointment is cancelled.
- [ ] Frontend: `Pay` redirects to Checkout; success and cancel return pages; visible test mode label with a test card.
- [ ] Register the production webhook endpoint and add the Stripe secrets to the server. Local webhooks go through the Stripe CLI.

## 8. Chat

- [ ] Schema: `conversations` (client and specialist) and `messages`.
- [ ] REST: conversation list, paginated history, sending. Access for participants only, after the first booking request.
- [ ] Real time: WebSocket with STOMP, JWT authentication on connect, reconnection on the client, WebSocket upgrade in the reverse proxy.
- [ ] Unread count in the sidebar and read state.
- [ ] "Message" action on bookings and "Messages" in the sidebar.

Out of scope: attachments, group chats, typing indicators, email notifications.

## 9. Google registration

- [ ] `POST /auth/google`: known users sign in; unknown users get a "registration required" answer with name and email, and no account is created.
- [ ] `POST /auth/google/register`: verify the Google credential again and create the account. The Google ID comes from the verified token, not from the form.
- [ ] Completion page: name from Google, read-only email, account type, optional phone. The credential stays in memory; abandoning the form leaves nothing in the database.
- [ ] Remove the implicit `CLIENT` account creation.

## 10. Password reset and email notifications

- [ ] Email delivery through a transactional email provider; verify the sending domain with SPF and DKIM.
- [ ] `POST /auth/forgot-password` always answers the same way; a random one-time token is stored as a hash with an expiry.
- [ ] `POST /auth/reset-password` sets the password, consumes the token and revokes refresh tokens. Rate limit both endpoints.
- [ ] Frontend: request form and new password page; restore the "Forgot password" link.
- [ ] Notifications sent after the transaction commits and asynchronously, so a mail failure never breaks a booking.
- [ ] Events: new request to the specialist; accepted with payment link and deadline, rejected and payment expired to the client; confirmed to both; cancelled to the other party.
- [ ] Skip demo addresses such as `@citeria.test`.

## 11. Help

- [ ] Static FAQ describing the working flow, with a support email link.

# Done

- [x] Avatars: upload, replace and delete in the profile page.
- [x] Public home page with responsive header, footer and light and dark themes.
- [x] Public catalog with filters and sorting.
- [x] Rate limits on login, registration and public catalog reads; the client address comes from the servlet container, so the reverse proxy must set `X-Forwarded-For`.
