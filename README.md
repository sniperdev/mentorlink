# MentorLink

A tutor-matching platform, built as a small set of independent services rather than
one monolith — mainly as a place to practice designing service boundaries, auth
between services, and a typed API layer end to end.

> **Status: in progress.** The backend (auth + tutor/subject domain) is functional
> and tested. The frontend currently covers browsing tutors only — there is no
> login/registration UI or booking flow yet. See [Roadmap](#roadmap).

## Architecture

```
                         ┌──────────────────────┐
                         │   Next.js frontend    │
                         │   (apps/mentorlink-    │
                         │    frontend)           │
                         └───────────┬───────────┘
                                     │ REST (JSON)
              ┌──────────────────────┼──────────────────────┐
              │                      │                      │
   ┌──────────▼──────────┐                       ┌──────────▼──────────┐
   │     auth-service      │                       │  mentorlink-backend   │
   │  (Spring Boot, :8081)  │                       │  (Spring Boot, :8080)  │
   │                       │                       │                       │
   │  - register / login   │   issues JWT   ─────▶  │  validates JWT as an  │
   │  - JWT access tokens  │                       │  OAuth2 resource      │
   │  - refresh tokens     │                       │  server               │
   │    (httpOnly cookies) │                       │  - tutor profiles     │
   └──────────┬───────────┘                       │  - subjects           │
              │                                    └──────────┬───────────┘
              │                                               │
              ▼                                               ▼
      ┌───────────────┐                              ┌───────────────┐
      │   auth_db      │                              │   tutor_db      │
      └───────────────┘                              └───────────────┘
                         (one PostgreSQL instance, one database per service)
```

`auth-service` is the only service that knows how to authenticate a user. It issues
short-lived JWT access tokens and longer-lived refresh tokens (stored server-side,
delivered as httpOnly cookies). `mentorlink-backend` never sees a password — it only
validates the JWTs it's handed, as a standard OAuth2 resource server. Each service
owns its own database (`auth_db`, `tutor_db`) inside a shared Postgres instance, so
the two stay independently deployable and don't reach into each other's tables.

## Tech stack

| Layer         | Stack                                                                 |
|---------------|------------------------------------------------------------------------|
| Frontend      | Next.js 16 (App Router), React 19, TypeScript, TanStack Query, Tailwind, shadcn/ui |
| Auth service  | Java 21, Spring Boot, Spring Security, JJWT, Flyway, PostgreSQL         |
| Backend       | Java 21, Spring Boot, Spring Security (OAuth2 resource server), Flyway, PostgreSQL, springdoc-openapi |
| Infra (local) | Docker Compose (PostgreSQL 17)                                         |
| Testing       | JUnit 5, Mockito, AssertJ, Spring Boot Test (unit + integration)       |

## Project structure

```
mentorlink/
├── apps/
│   └── mentorlink-frontend/     # Next.js app (public tutor browsing pages)
├── services/
│   ├── auth-service/            # registration, login, JWT + refresh tokens
│   └── mentorlink-backend/      # tutor profiles & subjects domain, JWT-protected
├── docker/
│   └── postgres/init/           # creates auth_db and tutor_db on first boot
└── docker-compose.yml           # local PostgreSQL instance
```

## Running locally

**Prerequisites:** Java 21, Node 20+, Docker.

1. **Start PostgreSQL**

   ```bash
   docker compose up -d
   ```

   This boots Postgres 17 and creates the `auth_db` and `tutor_db` databases.
   Each service then applies its own Flyway migrations on startup.

2. **Start `auth-service`** (port `8081`)

   ```bash
   cd services/auth-service
   JWT_SECRET=<any-256-bit-secret> ./mvnw spring-boot:run
   ```

3. **Start `mentorlink-backend`** (port `8080`)

   ```bash
   cd services/mentorlink-backend
   JWT_SECRET=<same-secret-as-above> ./mvnw spring-boot:run
   ```

   Both services must share the same `JWT_SECRET` so the backend can verify tokens
   issued by the auth service. API docs (springdoc/Swagger UI) are available once
   the backend is running.

4. **Start the frontend** (port `3000`)

   ```bash
   cd apps/mentorlink-frontend
   npm install
   npm run dev
   ```

## Tests

```bash
cd services/auth-service && ./mvnw test
cd services/mentorlink-backend && ./mvnw test
```

Covers JWT issuing/validation, the auth flow (register/login), and the
tutors/subjects services and controllers (unit tests with Mockito, plus an
integration test for the JWT authentication filter).

## Roadmap

- [ ] Login / registration UI in the frontend, wired to `auth-service`
- [ ] Booking flow between students and tutors
- [ ] Tutor-side dashboard (manage profile, availability)
- [ ] Deployed demo environment
