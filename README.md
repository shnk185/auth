# Auth Service

A session based auth service. Supports **email signup with OTP
verification** and **OAuth login**. Issues short lived JWT access tokens and
rotating opaque refresh tokens, both delivered as `HttpOnly` cookies. MySQL is the
persistent source of truth, Redis holds ephemeral state (OTPs, logout markers).

## Stack

- Java 17, Spring Boot 4
- MySQL 8
- Redis 7
- OAuth
- SMTP mail for OTP delivery

## Requirements
* java 17+
* docker 27+ (for mysql and redis)

## Setup instructions
```
git clone <this-repo>
cd auth
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=local
```
The app starts on **http://localhost:8080**.

## API summary

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/users` | Public | Sign up with username, email, password. Creates an unverified user and emails a 6 digit OTP |
| POST | `/users/verify` | Public | Verify OTP with user id + code. Activates the account |
| POST | `/users/sessions/email` | Public | Log in with username or email + password. Sets access + refresh cookies |
| GET | `/users/sessions/oauth/zalo` | Public | Redirect to the OAuth permission page (starts a login attempt) |
| GET | `/users/sessions/oauth/zalo/callback` | Public | OAuth callback (`code`, `state`, `code_challenge`). Sets cookies and redirects to the frontend |
| POST | `/users/sessions/refresh` | Public (valid refresh token) | Rotate refresh token, reissue access + refresh cookies |
| DELETE | `/users/sessions` | Cookie | Log out, revoke refresh token, clear cookies |
| GET | `/users` | Cookie | Fetch current user info |
| DELETE | `/users` | Cookie | Soft-delete own account + log out |


## Design decisions

* Short JWT access token + opaque refresh token: The access token is a
  self contained signed JWT (user id as subject, short TTL). The refresh token
  is a random value stored only as a SHA 256 hash in MySQL with expiry and
  revocation timestamps, so a database leak does not yield usable tokens.

* Single active refresh token with rotation: Issuing a new refresh token
  revokes the previous one. Refresh and re login both clear any logout marker,
  while logout writes a marker and revokes the refresh token.

* Cookie based sessions with a Redis logout check: Both tokens travel in
  `HttpOnly`, `Secure`, `SameSite=Lax` cookies. Every authenticated request
  validates the JWT signature and expiry, then rejects it if a per user logout
  marker exists in Redis. This gives near immediate logout without a per request
  database lookup.

* OTP in Redis with a bounded attempt counter: The signup code lives in
  Redis with a 24 hour TTL. Wrong codes decrement a separate attempts counter;
  exhausting it deletes the code so it cannot be brute forced indefinitely.
  Email sending is offloaded to a background executor so signup does not block
  on SMTP.

* OAuth login is auto provisioning with a state bound verifier: Starting a
  OAuth login stores a random code verifier in Redis keyed by a generated login
  UUID (`state`), with a short TTL. The callback validates the returned challenge
  against the stored verifier before exchanging the code and fetching the OAuth
  profile. Unknown OAuth ids get a new verified user on first login; known ids
  reuse the linked user.

* MySQL for durable, Redis for disposable: Users, credentials, Oauth links,
  and refresh token hashes live in MySQL. Anything that can expire or be
  regenerated (OTPs, logout flags, Oauth verifiers) lives in Redis with a TTL.

* Soft delete preserves history: Account deletion flags the user row rather
  than removing it, after forcing a logout so existing cookies stop working.
