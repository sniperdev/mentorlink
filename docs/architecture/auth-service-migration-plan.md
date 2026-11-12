# MentorLink – plan jednorazowej migracji `auth-service` → `mentorlink-backend`

> **Status:** plan do wykonania, 2026-11-12. Niczego jeszcze nie zaimplementowano.
> **Podstawa:** rzeczywisty kod na gałęzi `docs/architecture-discovery` (commit `cb995f7`) – wszystkie ścieżki, klasy i wartości poniżej zostały sprawdzone w repozytorium. Zgodnie z `target-architecture.md` (AD-2, AD-3, AD-5) kierunek jest już zatwierdzony; ten dokument jest instrukcją wykonania, nie kolejną analizą.
> **Jak używać:** każde zadanie ma identyfikator `M-xx`. Możesz wskazać zadanie („zrób M-06”) – dokument zawiera kontekst potrzebny do jego realizacji.
> **Ograniczenie weryfikacji:** w środowisku, w którym powstał plan, nie ma Javy ani Mavena, więc **żadna komenda ani fragment kodu nie były kompilowane**. Miejsca wymagające sprawdzenia przy pierwszym uruchomieniu oznaczono **[SPRAWDŹ]**. Rzeczy niemożliwe do ustalenia z repozytorium (konfiguracja Coolify, runner Forgejo) zebrano w §3.

---

## 1. Decyzje przyjęte w planie

Decyzje poniżej są minimalnym zestawem potrzebnym do połączenia serwisów. Wszystko, co z `target-architecture.md` wykracza poza to (18+, strefy czasowe, `GRANT_ADMIN`, rate limiting, anonimizacja, Spring Modulith), jest świadomie **poza zakresem** (§10).

| # | Decyzja | Uzasadnienie |
|---|---|---|
| D1 | Jeden backend `services/mentorlink-backend`, pakiet główny **zostaje** `com.sniperdev.mentorlink_backend`; nowy pakiet `identity` obok `tutors` i `subjects`. | Zmiana nazwy pakietu głównego (target-architecture proponuje `com.sniperdev.mentorlink`) to ~50 plików zmian bez związku z migracją. |
| D2 | Sesje: **Spring Security + Spring Session JDBC** w PostgreSQL. JWT, refresh tokeny, `jjwt`, `oauth2-resource-server` – usunięte. | Zgodnie z AD-3; natychmiastowe unieważnianie sesji, brak zarządzania kluczami. |
| D3 | Endpointy tożsamości pod **`/api/v1/auth/**`** i **`/api/v1/users/me/**`** (dziś `/auth/**` na porcie 8081). | Istniejący rewrite Next.js (`next.config.ts`) przekazuje tylko `/api/v1/:path*`. Dzięki prefiksowi frontend ma jeden origin i nic nie trzeba dopisywać w proxy. |
| D4 | Jedna baza **`mentorlink`**, schematy: `identity`, `tutoring` (tabela `tutor_profiles`), `catalog` (tabela `subjects`). Jedna historia Flyway (w `public`). Jeden użytkownik DB (`mentorlink`). | AD-5. Dane są testowe – tworzymy od zera, bez migracji danych. |
| D5 | Tabela użytkowników zostaje `identity.users` (nie `account`), a pojedyncza kolumna `role` jest **zastąpiona zbiorem ról** `identity.user_roles`. | Aktywacja roli Tutora ma *dodać* rolę (konto jest Studentem i Tutorem – BR-ID-1), a nie zamienić `STUDENT` na `TUTOR`. |
| D6 | Enum statusu zostaje: `ACTIVE`, `INACTIVE`, `BLOCKED`. **Tylko `ACTIVE` może się logować i korzystać z API.** | Nie przemianowujemy istniejącego modelu; `BLOCKED` = „zawieszone konto”. |
| D7 | Publiczne kontrakty modułu `identity` w pakiecie `identity.api`: `CurrentUser`, `CurrentUserProvider`, `UserRole`, `UserDirectory`. Pakiet `security` jest **usuwany w całości**. | Jedno miejsce, z którego inne moduły pobierają użytkownika; bez nowych abstrakcji poza tymi czterema. |
| D8 | Brak CORS (`WebConfig` usuwamy). Przeglądarka rozmawia tylko z frontendem (`/api/v1/*`), Next.js proxuje do backendu. | Cookie sesyjne + CSRF działają prosto tylko na jednym originie. Obecny CORS i tak nie działa dla zalogowanych (patrz F-9). |
| D9 | Dane testowe w `auth_db` / `tutor_db` **porzucamy**; bazy zostają nietknięte do końca weryfikacji (ułatwia rollback). | Wymóg użytkownika + minimalne ryzyko. |
| D10 | Testy integracyjne na **Testcontainers PostgreSQL** z prawdziwym Flyway; H2 usuwamy. | AD-14; schematy i `timestamptz` nie działają wiarygodnie na H2. |

---

## 2. Stan faktyczny (wynik analizy kodu)

### 2.1. Co jest w `auth-service` (`services/auth-service`, port 8081, baza `auth_db`)

| Obszar | Pliki (pod `src/main/java/com/sniperdev/auth_service/`) | Co robi |
|---|---|---|
| Endpointy | `auth/controller/AuthController.java` | `POST /auth/register` → 201, `POST /auth/login` → 204. **Brak** logout, refresh, `/me`. |
| Logika | `auth/service/AuthService.java` | Rejestracja zawsze jako `STUDENT`; login: hasło BCrypt, potem JWT + refresh token w cookie. **Nie sprawdza `status`.** |
| JWT | `auth/jwt/JwtService.java`, `JwtProperties.java`, `config/JwtConfig.java` | HS256, claims: `sub`=userId, `role`; TTL 15 min. |
| Refresh | `auth/refresh/**` (`RefreshToken`, repo, `RefreshTokenService`, `...Generator`, `...Hasher`, `CookieService`) | Zapisuje hash SHA-256 tokenu w `refresh_tokens`. **Nigdy go nie odczytuje ani nie waliduje** – nie ma endpointu odświeżania. |
| Filtr | `auth/security/JwtAuthenticationFilter.java` | Czyta **wyłącznie nagłówek `Authorization: Bearer`**. |
| Security | `config/SecurityConfig.java` | CSRF wyłączony, STATELESS, `/auth/**` i `/actuator/health` publiczne, `BCryptPasswordEncoder`. |
| Model | `users/model/User.java` (+ `UserRole`: STUDENT, TUTOR, ADMIN; `UserStatus`: ACTIVE, INACTIVE, BLOCKED), `UserRepository`, `UserService`, `users/dto/UserResponse.java` (**nieużywany**) | Tabela `users` (kolumna `role`, `status`). |
| Walidacja | `auth/dto/RegisterRequest.java`, `LoginRequest.java` | Tylko `@NotBlank`/`@Email`. **Brak ograniczenia długości hasła.** `AuthenticationResponse.java` – nieużywany. |
| Wspólne | `common/audit/AuditableEntity`, `common/exception/*`, `config/JpaAuditioningConfig` | Duplikaty tych z backendu (+ `InvalidCredentialsException` → 401). |
| DB | `db/migration/V1__users_table.sql`, `V2__refresh_tokens_table.sql` | `users` (`TIMESTAMPTZ`), `refresh_tokens` (`TIMESTAMP` bez strefy). |
| Testy | `AuthControllerTest`, `JwtServiceTest`, `JwtAuthenticationFilterIntegrationTest`, `AuthServiceTest`, `AuthServiceApplicationTests` | Wszystkie dotyczą JWT/cookies; H2, Flyway wyłączony (`src/test/resources/application-test.yaml`). |

### 2.2. Co jest w `mentorlink-backend` (`services/mentorlink-backend`, port 8080, baza `tutor_db`)

- `security/` – **8 klas JWT/resource-server**: `SecurityConfiguration`, `JwtConfiguration` (dekoder HS256 z `jwt.secret`), `JwtProperties`, `JwtAuthenticationConverter`, `JwtAuthenticationConverterConfiguration` (martwy kod – metoda nie jest `@Bean`), `SecurityCurrentUserProvider` (rzutuje na `JwtAuthenticationToken`), oraz kontrakty `CurrentUser(UUID id, UserRole role)`, `CurrentUserProvider`, `UserRole` (kopia enuma z auth-service).
- Reguły dostępu: `GET /api/v1/subjects/**` i `GET /api/v1/tutors/**` publiczne, `/actuator/health` publiczny, reszta wymaga uwierzytelnienia. Metody: `@PreAuthorize("hasRole('ADMIN')")` na `SubjectController.createSubject`, `@PreAuthorize("hasRole('TUTOR')")` na `TutorProfileController.createTutorProfile` (włączone `@EnableMethodSecurity`).
- Jedyny konsument `CurrentUserProvider`: `tutors/service/TutorProfileService.java` (bierze `userId` z sesji → brak możliwości podszycia; brak innych kontroli własności, bo nie ma jeszcze endpointów edycji).
- `tutor_profiles.user_id` to goły `UUID` (bez FK, bo był w innej bazie).
- `config/WebConfig.java`: CORS dla `/api/**` z `http://localhost:3000`, `allowCredentials(true)`.
- `config/OpenApiConfig.java` + springdoc 3.0.2.
- Migracje: `V1__create_tutor_profiles.sql`, `V2__create_subjects.sql` (bez schematu).
- Testy: `@WebMvcTest` + `@AutoConfigureMockMvc(addFilters = false)` (kontrolery), Mockito (serwisy), `MentorlinkBackendApplicationTests` (H2, Flyway wyłączony, `jwt.secret` w `application-test.yaml`).

### 2.3. Infrastruktura

| Element | Plik | Stan |
|---|---|---|
| Postgres lokalnie | `docker-compose.yml` | Tylko Postgres 17 (`mentorlink/mentorlink`, `POSTGRES_DB=postgres`), montuje `docker/postgres/init`. |
| Pełny stack | `docker-compose.coolify.yml` | Serwisy: `postgres`, `mentorlink-backend` (`${BACKEND_PORT:-18081}:8080`), `auth-service` (`${AUTH_PORT:-18082}:8081`), `frontend` (`${FRONTEND_PORT:-18080}:3000`). Zmienne wymagane: `POSTGRES_PASSWORD`, `JWT_SECRET`. Brak `healthcheck` dla aplikacji. |
| Inicjalizacja DB | `docker/postgres/init/init.sql` | `CREATE DATABASE auth_db; CREATE DATABASE tutor_db;` |
| CI | `.forgejo/workflows/auth.yml`, `backend.yml`, `frontend.yml` | Każdy: testy → `docker build` → push do `forgejo.home.sniperlab.uk/sniperdev/mentorlink-{auth,backend,frontend}` (przez `tcp://docker-in-docker:2375`) → `curl` webhook Coolify (`secrets.COOLIFY_WEBHOOK_URL_{AUTH,BACKEND,FRONTEND}` + `COOLIFY_TOKEN`) tylko poza PR. |
| Dockerfile backendu | `services/mentorlink-backend/Dockerfile` | Multi-stage, Temurin 21, `./mvnw verify`, EXPOSE 8080, user nie-root. Obraz JRE zawiera `curl` i `wget` (sprawdzone). |
| Frontend | `apps/mentorlink-frontend/Dockerfile`, `next.config.ts` | Rewrite `/api/v1/:path*` → `${BACKEND_INTERNAL_URL:-http://mentorlink-backend:8080}`; `NEXT_PUBLIC_API_URL` (domyślnie `/api/v1`) i `BACKEND_INTERNAL_URL` są **wpiekane w build**. |

### 2.4. Frontend (`apps/mentorlink-frontend`, Next.js 16.2.10, React 19, TanStack Query, axios, zod)

- **Zero integracji z `auth-service`.** Nie ma żadnej referencji do portu 8081, `/auth`, cookies, logowania. `src/lib/api/client.ts` to `axios.create({ baseURL: NEXT_PUBLIC_API_URL })` bez `withCredentials`.
- `src/components/layout/Navbar.tsx`: przyciski „Log in” / „Sign up” są martwe (bez linków i handlerów). Nie ma stron logowania/rejestracji, nie ma stron chronionych (jedyne trasy: `(public)/page.tsx`, `(public)/tutors/[id]/page.tsx`).
- Wywołania API: tylko `GET /tutors`, `GET /tutors/{id}` (`src/features/tutors/api/tutor.api.ts`).

### 2.5. Rozbieżności i błędy wykryte w kodzie (ważne dla planu)

| ID | Znalezisko | Konsekwencja dla migracji |
|---|---|---|
| F-1 | `AuthService` ustawia JWT w cookie `access_token`, ale **ani** `JwtAuthenticationFilter`, **ani** resource server backendu nie czytają cookie – oba oczekują nagłówka `Authorization: Bearer`. | Obecny przepływ cookie→API **nigdy nie działał end-to-end** (README/code-map opisują go jako działający). Nie ma zachowania do „zachowania” – nowe sesje to pierwsza działająca wersja. |
| F-2 | Refresh tokeny są zapisywane, ale nigdy nie używane; brak logout/refresh/me. | Cała gałąź `auth/refresh/**` i tabela `refresh_tokens` odpadają bez zastępstwa. |
| F-3 | Login nie sprawdza `UserStatus`. | Zawieszone konto dziś loguje się bez przeszkód → naprawiane w M-06 (wymaganie bezpieczeństwa). |
| F-4 | Rejestracja zawsze `STUDENT`; nie istnieje żadna ścieżka do roli `TUTOR` ani `ADMIN`. | `POST /tutors/profile` i `POST /subjects` są dziś **nieosiągalne**. Plan dodaje aktywację Tutora (M-06); `ADMIN` ustawiamy ręcznie w SQL (§8 i §9). |
| F-5 | Frontend `TutorSummary` oczekuje `email`, `firstName`, `lastName`, a backend zwraca `TutorSummaryResponse(id, userId, bio)`. `TutorCard.tsx` robi `tutor.firstName[0]` → **TypeError**, gdy lista tutorów nie jest pusta. | Błąd niezależny od migracji, ale scalenie daje naturalny sposób naprawy (M-11). Przy okazji: pole `email` nie może być publiczne. |
| F-6 | `docker-compose.coolify.yml` buduje frontend z `NEXT_PUBLIC_API_URL=${FRONTEND_API_URL:-http://localhost:18081/api/v1}` (absolutny, inny origin), a `frontend.yml` buduje z `/api/v1`. | Dwa różne tryby; absolutny URL zepsułby cookies/CSRF. Ujednolicamy do `/api/v1` (M-13). |
| F-7 | Cookies mają `Secure=true`, a środowiska dev/LAN bywają HTTP (CM-R12 w code-map). | Chrome/Firefox akceptują `Secure` na `localhost` po HTTP, Safari nie. Na LAN wymagany TLS na proxy. |
| F-8 | `init.sql` wykonuje się **tylko przy pustym wolumenie** Postgresa. | Istniejąca baza (lokalna i w Coolify) nie dostanie `mentorlink` automatycznie – wymaga ręcznego `CREATE DATABASE` (M-02). |
| F-9 | `WebConfig` dodaje CORS przez Spring MVC, ale Spring Security nie ma `.cors()`, więc preflight `OPTIONS` jest odrzucany przed MVC. | CORS nigdy nie działał dla zalogowanych; usuwamy (D8). |
| F-10 | `/swagger-ui/**` i `/v3/api-docs/**` mieszczą się w `anyRequest().authenticated()`. | README twierdzi, że Swagger jest dostępny; po zmianie też wymagałby sesji + CSRF. Nie zmieniamy (poza zakresem) – tylko notatka. |
| F-11 | Tworzone są dwie kopie `UserRole`, `AuditableEntity`, `ApiError`, `ConflictException`, `ResourceNotFoundException`, `GlobalExceptionHandler`, `JpaAuditioningConfig`. | Przy scalaniu zostają wersje backendu, kopie z auth-service nie są przenoszone. |
| F-12 | `auth-service/application.yaml` ma `spring.application.name: mentorlink-backend` (kopiuj-wklej). | Bez znaczenia po usunięciu serwisu. |
| F-13 | `pom.xml` auth-service i backendu: oba Spring Boot 4.0.7, Java 21, Lombok, `spring-boot-starter-flyway`. Backend dodatkowo: springdoc 3.0.2, `oauth2-resource-server`. Backendowy `SubjectControllerTest` importuje `com.fasterxml.jackson...` (Jackson 2, z springdoc), a testy auth-service `tools.jackson...` (Jackson 3, domyślny w Boot 4). | Nowy kod i testy: używaj `tools.jackson.databind.ObjectMapper`. |
| F-14 | Dokumentacja (`README.md`, `docs/code-map.md`) opisuje stan sprzed migracji i F-1 jako działające. | Aktualizacja w M-16. |

---

## 3. Fakty, których nie da się ustalić z repozytorium (sprawdź przed startem – M-00)

1. **Jak Coolify wdraża usługi?** Repo zawiera `docker-compose.coolify.yml`, ale CI buduje obrazy i uderza w **trzy osobne webhooki** (`..._AUTH`, `..._BACKEND`, `..._FRONTEND`). Sprawdź w UI Coolify: czy to jeden zasób „Docker Compose”, czy trzy osobne „Applications” z obrazem z rejestru? Od tego zależy M-15 (czy usuwa się zasób, czy serwis z compose'a) i czy `http://mentorlink-backend:8080` jest osiągalne z frontendu.
2. **Domeny / reverse proxy:** jakie domeny są przypisane (frontend? backend? auth?). Cel: tylko domena frontendu; backend i baza bez publicznej domeny.
3. **Czy proxy Coolify terminuje TLS** także dla dostępu z LAN (potrzebne dla `Secure` cookie).
4. **Czy runner Forgejo ma dostęp do Dockera dla Testcontainers.** Workflow używa `DOCKER_HOST=tcp://docker-in-docker:2375` wyłącznie w krokach `docker`; kroki `./mvnw test` dotąd go nie potrzebowały. Plan zakłada ustawienie `DOCKER_HOST` także dla testów (M-14) – **[SPRAWDŹ]**. Awaryjnie: `services: postgres` w workflow (opis w M-14).
5. **Wolumen Postgresa w Coolify** – czy można go bezpiecznie zachować (tak, plan nie dotyka `auth_db`/`tutor_db`).

---

## 4. Docelowa struktura

```text
services/mentorlink-backend/
├── pom.xml                                           # M-03
├── src/main/resources/
│   ├── application.yaml                              # M-02, M-05
│   └── db/migration/
│       ├── V1__create_schemas.sql                    # nowy
│       ├── V2__identity_users.sql                    # nowy (users, user_roles)
│       ├── V3__identity_spring_session.sql           # nowy
│       ├── V4__tutoring_tutor_profiles.sql           # przepisany ze starego V1
│       └── V5__catalog_subjects.sql                  # przepisany ze starego V2
├── src/main/java/com/sniperdev/mentorlink_backend/
│   ├── MentorlinkBackendApplication.java
│   ├── common/{audit,exception}/                     # + AccountSuspendedException, rozszerzony GlobalExceptionHandler
│   ├── config/{JpaAuditioningConfig,OpenApiConfig}.java     # WebConfig usunięty
│   ├── identity/
│   │   ├── api/            CurrentUser, CurrentUserProvider, UserRole, UserDirectory, UserSummary
│   │   ├── controller/     AuthController, CurrentUserController
│   │   ├── dto/            RegisterRequest, LoginRequest, CurrentUserResponse
│   │   ├── model/          User, UserStatus
│   │   ├── repository/     UserRepository
│   │   ├── service/        AuthService, UserService (implementuje UserDirectory)
│   │   └── security/       SecurityConfiguration, AccountPrincipal, AccountUserDetailsService,
│   │                       SessionAccountFilter, SessionCurrentUserProvider
│   ├── subjects/ …       (bez zmian poza @Table(schema = "catalog"))
│   └── tutors/ …         (zmiany: @Table(schema), import CurrentUser, M-11)
└── src/test/…            # M-10
```

Kolejność zależności: `tutors` → `identity.api`; `identity` **nie** zależy od `tutors`/`subjects`. Nie ma zależności cyklicznych.

---

## 5. Kolejność operacji (jedna migracja, jedna gałąź)

Wszystko robione na jednej gałęzi `migration/auth-into-backend`; nic nie jest wdrażane do M-15. Stary `auth-service` działa nietknięty do M-18.

```text
M-00 pre-flight  →  M-01 gałąź+baseline  →  M-02 DB/Flyway  →  M-03 pom  →  M-04 kod Identity
→ M-05 Spring Security+Session+CSRF  →  M-06 endpointy auth  →  M-07 CurrentUser + tutors
→ M-08 błędy  →  M-09 usunięcie JWT  →  M-10 testy backendu  →  M-11 TutorSummary
→ M-12 frontend  →  M-13 Docker/compose  →  M-14 CI  →  M-15 Coolify  →  M-16 docs
→ M-17 test całego systemu  →  M-18 wyłączenie auth-service
```

---

## 6. Zadania

### M-00. Pre-flight (tylko Ty)

- **Co:** odpowiedz na pytania z §3 (UI Coolify, runner Forgejo). Zapisz wyniki w komentarzu PR.
- **Dlaczego:** M-14 i M-15 zależą od odpowiedzi.
- **Weryfikacja:** masz odpowiedzi na pkt 1–4 z §3.

### M-01. Gałąź i stan odniesienia

- **Co:** utwórz gałąź i potwierdź, że obie usługi dziś budują się i testy przechodzą.
- **Dlaczego:** wiedza, co było zepsute *przed* migracją.
- **Komendy:**
  ```bash
  git checkout -b migration/auth-into-backend      # z main po zmergowaniu docs, inaczej z docs/architecture-discovery
  (cd services/auth-service && ./mvnw -B test)
  (cd services/mentorlink-backend && ./mvnw -B test)
  ```
- **Weryfikacja:** oba zestawy testów zielone (lub zapisane znane błędy).

### M-02. Baza `mentorlink`, schematy, Flyway

- **Co / dlaczego:** wspólna baza, trzy schematy (D4), migracje od zera.
- **Pliki do zmiany:**
  - `docker/postgres/init/init.sql` → zamień zawartość na:
    ```sql
    CREATE DATABASE mentorlink OWNER mentorlink;
    ```
    (`auth_db`/`tutor_db` znikają z init; istniejące bazy zostają w wolumenie – patrz niżej).
  - `services/mentorlink-backend/src/main/resources/application.yaml`:
    - `spring.datasource.url` → `${DB_URL:jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:mentorlink}}` (domyślna nazwa `mentorlink` zamiast `tutor_db`),
    - usuń blok `jwt:` (M-09).
- **Migracje** w `services/mentorlink-backend/src/main/resources/db/migration/` (baza nowa, więc historię można przepisać; checksumy nie mają znaczenia):
  - `V1__create_schemas.sql`:
    ```sql
    CREATE SCHEMA IF NOT EXISTS identity;
    CREATE SCHEMA IF NOT EXISTS tutoring;
    CREATE SCHEMA IF NOT EXISTS catalog;
    ```
  - `V2__identity_users.sql`:
    ```sql
    CREATE TABLE identity.users (
        id            UUID PRIMARY KEY,
        email         VARCHAR(255) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        first_name    VARCHAR(255) NOT NULL,
        last_name     VARCHAR(255) NOT NULL,
        status        VARCHAR(30)  NOT NULL,
        created_at    TIMESTAMPTZ  NOT NULL,
        updated_at    TIMESTAMPTZ  NOT NULL
    );

    CREATE TABLE identity.user_roles (
        user_id UUID        NOT NULL REFERENCES identity.users (id) ON DELETE CASCADE,
        role    VARCHAR(30) NOT NULL,
        PRIMARY KEY (user_id, role)
    );
    ```
  - `V3__identity_spring_session.sql` – schemat Spring Session JDBC z prefiksem `identity.` (kopia `org/springframework/session/jdbc/schema-postgresql.sql` z jara `spring-session-jdbc`, **[SPRAWDŹ]** zgodność z wersją, którą wciągnie Boot 4.0.7):
    ```sql
    CREATE TABLE identity.spring_session (
        primary_id            CHAR(36) NOT NULL,
        session_id            CHAR(36) NOT NULL,
        creation_time         BIGINT   NOT NULL,
        last_access_time      BIGINT   NOT NULL,
        max_inactive_interval INT      NOT NULL,
        expiry_time           BIGINT   NOT NULL,
        principal_name        VARCHAR(100),
        CONSTRAINT spring_session_pk PRIMARY KEY (primary_id)
    );
    CREATE UNIQUE INDEX spring_session_ix1 ON identity.spring_session (session_id);
    CREATE INDEX spring_session_ix2 ON identity.spring_session (expiry_time);
    CREATE INDEX spring_session_ix3 ON identity.spring_session (principal_name);

    CREATE TABLE identity.spring_session_attributes (
        session_primary_id CHAR(36)     NOT NULL,
        attribute_name     VARCHAR(200) NOT NULL,
        attribute_bytes    BYTEA        NOT NULL,
        CONSTRAINT spring_session_attributes_pk PRIMARY KEY (session_primary_id, attribute_name),
        CONSTRAINT spring_session_attributes_fk FOREIGN KEY (session_primary_id)
            REFERENCES identity.spring_session (primary_id) ON DELETE CASCADE
    );
    ```
  - `V4__tutoring_tutor_profiles.sql` = stary `V1__create_tutor_profiles.sql` z `tutoring.tutor_profiles`.
  - `V5__catalog_subjects.sql` = stary `V2__create_subjects.sql` z `catalog.subjects`.
  - Usuń stare `V1__create_tutor_profiles.sql` i `V2__create_subjects.sql` (`git mv` + edycja zachowa historię).
  - Brak FK między schematami (`tutor_profiles.user_id` zostaje goły `UUID`) – zgodnie z AD-5.
- **Encje** (adnotacje schematu): `Subject` → `@Table(name = "subjects", schema = "catalog")`; `TutorProfile` → `@Table(name = "tutor_profiles", schema = "tutoring")`; `User` (M-04) → `schema = "identity"`.
- **Istniejący wolumen Postgresa (F-8) – ręcznie, jednorazowo:**
  ```bash
  # lokalnie
  docker exec -it mentorlink-postgres psql -U mentorlink -d postgres -c "CREATE DATABASE mentorlink OWNER mentorlink;"
  # Coolify: w terminalu kontenera postgres wykonaj to samo polecenie psql
  ```
  Alternatywa lokalna (bez zachowania danych): `docker compose down -v && docker compose up -d`.
- **Użytkownik DB:** aplikacja łączy się jako `mentorlink` – właściciel bazy, więc Flyway może tworzyć schematy i tabele. W obrazie `postgres` ten użytkownik (`POSTGRES_USER`) jest superużytkownikiem; osobna rola aplikacyjna bez uprawnień superusera jest w liście hardeningu (§10, H-2).
- **Do usunięcia:** wszelkie odwołania do `auth_db`, `tutor_db` (compose, README, init.sql).
- **Weryfikacja:**
  ```bash
  docker compose up -d postgres
  docker exec mentorlink-postgres psql -U mentorlink -d mentorlink -c '\dn'     # identity, tutoring, catalog
  ```
  Po M-10: uruchomienie aplikacji przeciw tej bazie kończy się bez błędów `ddl-auto: validate`; `\dt identity.*` pokazuje `users`, `user_roles`, `spring_session*`.

### M-03. Zależności Maven (`services/mentorlink-backend/pom.xml`)

- **Dodaj:**
  - `org.springframework.boot:spring-boot-starter-session-jdbc` (**[SPRAWDŹ]** – artefakt `4.0.7` istnieje w Maven Central),
  - testowe: `org.springframework.boot:spring-boot-testcontainers`, `org.testcontainers:testcontainers-junit-jupiter`, `org.testcontainers:testcontainers-postgresql` (wersje z BOM-u Boot; **[SPRAWDŹ]** że BOM je zarządza – w razie braku dodaj `<version>`).
- **Usuń:** `spring-boot-starter-oauth2-resource-server`, `com.h2database:h2` (test).
- **Nie dodawaj:** `jjwt-*` (to był tylko w auth-service).
- **Weryfikacja:** `./mvnw -B -q dependency:tree | grep -E "jjwt|oauth2|h2"` → pusto; `./mvnw -B compile` przechodzi po M-04.

### M-04. Przeniesienie kodu Identity

Wszystkie ścieżki względem `services/mentorlink-backend/src/main/java/com/sniperdev/mentorlink_backend/`. Zmieniaj pakiet z `auth_service.…` na `mentorlink_backend.identity.…`, używaj `git mv` z `services/auth-service/…`, żeby zachować historię.

| Źródło (auth-service) | Akcja | Cel |
|---|---|---|
| `users/model/User.java` | **Przenieś + zmodyfikuj**: usuń pole `role`, dodaj zbiór ról (patrz niżej), `@Table(name="users", schema="identity")` | `identity/model/User.java` |
| `users/model/UserStatus.java` | Przenieś | `identity/model/UserStatus.java` |
| `users/model/UserRole.java` | **Nie przenoś** – używamy `security/UserRole.java` backendu, przesuniętego w M-07 | `identity/api/UserRole.java` |
| `users/repository/UserRepository.java` | Przenieś (`existsByEmail`, `findByEmail`) | `identity/repository/` |
| `users/service/UserService.java` | Przenieś + zmodyfikuj (rola jako zbiór, normalizacja e-maila, `addRole`) | `identity/service/UserService.java` |
| `auth/service/AuthService.java` | **Napisz od nowa** (M-06), nazwa zostaje | `identity/service/AuthService.java` |
| `auth/controller/AuthController.java` | Przenieś + przepisz (M-06) | `identity/controller/AuthController.java` |
| `auth/dto/RegisterRequest.java`, `LoginRequest.java` | Przenieś + zaostrz walidację | `identity/dto/` |
| `users/dto/UserResponse.java` | **Zastąp** rekordem `CurrentUserResponse` | `identity/dto/CurrentUserResponse.java` |
| `auth/dto/AuthenticationResponse.java` | **Usuń** (nieużywany) | — |
| `auth/jwt/**`, `config/JwtConfig.java` | **Usuń** | — |
| `auth/refresh/**` | **Usuń** (F-2) | — |
| `auth/security/JwtAuthenticationFilter.java` | **Usuń** | — |
| `config/SecurityConfig.java` | **Usuń**; `PasswordEncoder` przenosi się do nowego `SecurityConfiguration` (M-05) | — |
| `common/**`, `config/JpaAuditioningConfig.java` | **Usuń** (duplikaty, F-11) | — |
| `db/migration/*`, `application.yaml`, `Dockerfile`, `pom.xml`, `src/test/**` | **Usuń** razem z katalogiem w M-18 | — |

**Szczegóły `User`:**
```java
@ElementCollection(fetch = FetchType.EAGER)
@CollectionTable(name = "user_roles", schema = "identity",
                 joinColumns = @JoinColumn(name = "user_id"))
@Enumerated(EnumType.STRING)
@Column(name = "role", nullable = false, length = 30)
@Builder.Default
private Set<UserRole> roles = new HashSet<>();
```
(Zostaw `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor` jak w oryginale; usuń `@Column`/pole `role`.)

**Szczegóły `UserService`:**
- `createUser(email, passwordHash, firstName, lastName)` → `email.trim().toLowerCase(Locale.ROOT)`, `existsByEmail` → `ConflictException("User with this email already exists")`, role = `{STUDENT}`, status `ACTIVE`. Złapanie `DataIntegrityViolationException` na wyścigu (dwa równoległe rejestry) → też `ConflictException`.
- `addRole(UUID userId, UserRole role)` – idempotentne.
- `getById(UUID)` → `ResourceNotFoundException`.
- `getByEmail` nie rzuca już `InvalidCredentialsException` – logowanie obsługuje `AuthenticationManager` (M-06).

**Weryfikacja:** `./mvnw -B compile` – tylko błędy dotyczące klas, które powstaną w M-05/M-07.

### M-05. Spring Security: sesje, CSRF, uwierzytelnianie

- **Co:** jedna konfiguracja bezpieczeństwa w `identity/security/`, zastępująca `security/SecurityConfiguration.java` backendu i `config/SecurityConfig.java` auth-service.
- **Nowe pliki** (`identity/security/`):
  1. `SecurityConfiguration` (`@Configuration @EnableWebSecurity @EnableMethodSecurity`):
     - bean `PasswordEncoder` = `BCryptPasswordEncoder` (bez zmian względem auth-service),
     - bean `AuthenticationManager` = `authConfig.getAuthenticationManager()` (Spring zbuduje `DaoAuthenticationProvider` z `AccountUserDetailsService` + `PasswordEncoder`),
     - bean `SecurityContextRepository` = `HttpSessionSecurityContextRepository` (zapis kontekstu do sesji przy loginie – w Spring Security 6+ nie dzieje się to automatycznie przy własnym kontrolerze logowania),
     - `SecurityFilterChain`:
       ```java
       http
         .sessionManagement(s -> s.sessionCreationPolicy(IF_REQUIRED)
                                  .sessionFixation(f -> f.changeSessionId()))
         .securityContext(c -> c.securityContextRepository(securityContextRepository))
         .csrf(csrf -> csrf
             .csrfTokenRepository(cookieCsrfTokenRepository())   // XSRF-TOKEN, HttpOnly=false, Secure, SameSite=Lax
             /* tryb SPA – patrz uwaga */ )
         .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
         .logout(l -> l.logoutUrl("/api/v1/auth/logout")
                       .deleteCookies("SESSION")
                       .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
         .authorizeHttpRequests(a -> a
             .requestMatchers(POST, "/api/v1/auth/register", "/api/v1/auth/login").permitAll()
             .requestMatchers(GET, "/api/v1/subjects", "/api/v1/subjects/**",
                                   "/api/v1/tutors", "/api/v1/tutors/**").permitAll()
             .requestMatchers("/actuator/health").permitAll()
             .anyRequest().authenticated())
         .addFilterAfter(sessionAccountFilter, SecurityContextHolderFilter.class);
       ```
       **CSRF dla SPA [SPRAWDŹ]:** Spring Security 7 powinno mieć `csrf.spa()`; jeśli wersja zaciągnięta przez Boot 4.0.7 go nie ma, użyj ręcznego wzorca z dokumentacji Spring Security („Single-Page Applications”: `SpaCsrfTokenRequestHandler` + `CsrfCookieFilter`). Cel jest ten sam: cookie `XSRF-TOKEN` (czytelne dla JS) ustawiane na **każdej** odpowiedzi, a nagłówek `X-XSRF-TOKEN` weryfikowany na każdym `POST/PUT/PATCH/DELETE`.
       **Cookie CSRF:** `CookieCsrfTokenRepository.withHttpOnlyFalse()` + `setCookieCustomizer(c -> c.secure(true).sameSite("Lax"))` (w dev po HTTP przez nadpisanie, patrz niżej).
       **CSRF obowiązuje także dla `login` i `register`** (ochrona przed login-CSRF). Frontend dostaje cookie przy pierwszym `GET /auth/me` (które zwróci 401, ale cookie zostanie ustawione – test T-CSRF-3).
  2. `AccountPrincipal` – `implements UserDetails, CredentialsContainer, Serializable` z polami `UUID id`, `String email`, `String passwordHash`, `Set<UserRole> roles`; `getAuthorities()` → `ROLE_<ROLE>`; `getUsername()` → `id.toString()` (stabilny indeks `principal_name` w tabeli sesji); `eraseCredentials()` zeruje `passwordHash` – dzięki temu hasha **nie ma w serializowanej sesji**. Dodaj `serialVersionUID`. `isEnabled()` zawsze `true` (status sprawdzamy sami, patrz M-06 – inaczej Spring ujawniłby status konta *przed* sprawdzeniem hasła).
  3. `AccountUserDetailsService implements UserDetailsService` – `loadUserByUsername(email)`: `UserRepository.findByEmail(email.trim().toLowerCase())` → `AccountPrincipal`; brak → `UsernameNotFoundException` (Spring zamieni go na `BadCredentialsException`).
  4. `SessionAccountFilter extends OncePerRequestFilter` – dla uwierzytelnionej (nie anonimowej) sesji **przy każdym żądaniu** ładuje użytkownika po `id`; jeśli brak konta lub `status != ACTIVE` → `invalidate` sesji, wyczyść `SecurityContextHolder`, kontynuuj (endpoint chroniony zwróci 401); w przeciwnym razie podmień `Authentication` na świeże role z bazy. To daje: natychmiastowe skutki zawieszenia konta i natychmiastowe widzenie nowej roli (np. po aktywacji Tutora) – jedno zapytanie po PK na żądanie.
  5. `SessionCurrentUserProvider implements CurrentUserProvider` (M-07).
- **`application.yaml` (backend) – dodaj:**
  ```yaml
  server:
    forward-headers-strategy: framework        # za reverse proxy Coolify
    servlet:
      session:
        timeout: 2h                            # bezczynność; [SPRAWDŹ] czy Spring Session czyta tę wartość czy spring.session.timeout
        cookie:
          name: SESSION
          http-only: true
          secure: ${SESSION_COOKIE_SECURE:true}
          same-site: lax
          path: /
  spring:
    session:
      jdbc:
        table-name: identity.spring_session
        initialize-schema: never               # schemat tworzy Flyway (V3)
  ```
  `SESSION_COOKIE_SECURE=false` wolno ustawić wyłącznie lokalnie, gdy używasz Safari po HTTP. Dla `CookieCsrfTokenRepository` użyj tej samej właściwości (`@Value`).
- **Weryfikacja:** `./mvnw -B compile` przechodzi; test `T-CTX-1` (kontekst się wstaje na Postgresie) – po M-10.

### M-06. Endpointy: rejestracja, login, logout, `/me`, aktywacja Tutora

- **Plik:** `identity/controller/AuthController.java` (`@RequestMapping("/api/v1/auth")`) i `identity/controller/CurrentUserController.java` (`@RequestMapping("/api/v1/users/me")`).

| Metoda i ścieżka | Dostęp | Odpowiedź | Zachowanie |
|---|---|---|---|
| `POST /api/v1/auth/register` | publiczny + CSRF | `201` + `CurrentUserResponse` | tworzy konto (rola `STUDENT`, status `ACTIVE`) i **loguje** (jak dziś); `409` gdy e-mail zajęty; `400` walidacja |
| `POST /api/v1/auth/login` | publiczny + CSRF | `200` + `CurrentUserResponse` | `401` złe dane; `403` gdy `status != ACTIVE` (sprawdzone **po** poprawnym haśle) |
| `POST /api/v1/auth/logout` | sesja + CSRF | `204` | obsługiwane przez `http.logout(...)` z M-05: unieważnia sesję w DB i kasuje cookie |
| `GET /api/v1/auth/me` | sesja | `200` + `CurrentUserResponse` / `401` | dane z bazy (nie z sesji) |
| `POST /api/v1/users/me/roles/tutor` | sesja + CSRF | `200` + `CurrentUserResponse` | dodaje rolę `TUTOR` (idempotentne). Zmiana widoczna od następnego żądania dzięki `SessionAccountFilter` |

- **`AuthService` (nowy):**
  ```java
  public User register(RegisterRequest r, HttpServletRequest req, HttpServletResponse res) {
      User user = userService.createUser(r.email(), passwordEncoder.encode(r.password()), r.firstName(), r.lastName());
      signIn(user, req, res);
      return user;
  }
  public User login(LoginRequest r, HttpServletRequest req, HttpServletResponse res) {
      Authentication auth = authenticationManager.authenticate(
          UsernamePasswordAuthenticationToken.unauthenticated(r.email().trim().toLowerCase(Locale.ROOT), r.password()));
      User user = userService.getById(((AccountPrincipal) auth.getPrincipal()).getId());
      if (user.getStatus() != UserStatus.ACTIVE) throw new AccountSuspendedException();
      signIn(user, req, res);
      return user;
  }
  private void signIn(User user, HttpServletRequest req, HttpServletResponse res) {
      // 1. jeśli istnieje sesja: req.changeSessionId()   (ochrona przed session fixation)
      // 2. SecurityContext z UsernamePasswordAuthenticationToken(AccountPrincipal, null, authorities)
      // 3. SecurityContextHolder.setContext(ctx); securityContextRepository.saveContext(ctx, req, res);
  }
  ```
  Uwaga: `register` ma być `@Transactional`, `login` nie musi (jedno odczytanie). `AccountPrincipal` budujesz z `User` w jednym miejscu (statyczna fabryka).
- **DTO:**
  - `RegisterRequest`: `@NotBlank @Email @Size(max=255) email`; `@NotBlank @Size(min=8, max=72) password` (**72 = limit bajtów BCrypt**, dłuższe hasła są inaczej obcinane/odrzucane); `@NotBlank @Size(max=255) firstName`, `lastName`.
  - `LoginRequest`: bez zmian (`@NotBlank @Email email`, `@NotBlank password`) – celowo bez `@Size` na haśle logowania.
  - `CurrentUserResponse(UUID id, String email, String firstName, String lastName, Set<UserRole> roles, UserStatus status)` (record).
- **Uwagi:** komunikat 401 zawsze ten sam („Invalid email or password”) – bez rozróżniania „brak konta” / „złe hasło”.
- **Weryfikacja:** testy T-AUTH-* z §8; ręcznie `curl` w §9.3.

### M-07. `CurrentUser` i integracja z modułami

- **Co / dlaczego:** jedno miejsce odczytu aktualnego użytkownika; moduły nie znają Spring Security.
- **Zmiany:**
  - `git mv security/CurrentUser.java identity/api/CurrentUser.java` – zmiana kontraktu: `record CurrentUser(UUID id, Set<UserRole> roles)` oraz pomocnik `boolean hasRole(UserRole role)`.
  - `git mv security/CurrentUserProvider.java identity/api/CurrentUserProvider.java` (bez zmian).
  - `git mv security/UserRole.java identity/api/UserRole.java` (`STUDENT, TUTOR, ADMIN` – bez zmian).
  - `identity/security/SessionCurrentUserProvider.java` – czyta `SecurityContextHolder.getContext().getAuthentication().getPrincipal()` jako `AccountPrincipal`; brak/anonimowy → `AuthenticationCredentialsNotFoundException`.
  - `tutors/service/TutorProfileService.java` – tylko zmiana importów (`...security.*` → `...identity.api.*`); logika (`userId` z sesji, nie z requestu) zostaje.
  - `tutors/service/TutorProfileServiceTest.java` – importy + `new CurrentUser(userId, Set.of(UserRole.TUTOR))` w dwóch miejscach (linie ~67 i ~96).
- **Zasady autoryzacji (bez duplikowania):**
  1. *Rola* – wyłącznie `@PreAuthorize("hasRole('…')")` na metodzie kontrolera (jak dziś: `SubjectController`, `TutorProfileController`). Nie sprawdzaj ról ręcznie w serwisach.
  2. *Własność zasobu* – w serwisie: `currentUserProvider.getCurrentUser().id()` porównywany z `resource.getUserId()`; nigdy `userId` z body/URL. Dziś istnieje jedno takie miejsce; nowe endpointy edycji kopiują ten wzorzec.
  3. *Status konta* – egzekwowany centralnie w `SessionAccountFilter`; moduły niczego nie sprawdzają.
- **Weryfikacja:** `rg "mentorlink_backend.security" services/mentorlink-backend` → brak wyników (po M-09).

### M-08. Błędy i odpowiedzi HTTP

- **Plik:** `common/exception/GlobalExceptionHandler.java` (backend) – dodaj:
  - `BadCredentialsException` → `401 Unauthorized`, message `"Invalid email or password"`,
  - `AccountSuspendedException` (nowa klasa w `common/exception/`) → `403 Forbidden`, message `"Account is suspended"`.
- **Pozostałe odpowiedzi:** `401` z `HttpStatusEntryPoint` (puste body), `403` – domyślny `AccessDeniedHandler` (puste body; dotyczy złej roli oraz brakującego/nieprawidłowego CSRF). `409` i `400` istnieją.
- **Dlaczego bez JSON-a dla 401/403:** frontend potrzebuje tylko kodu statusu; unikamy dodatkowej klasy.
- **Weryfikacja:** T-AUTH-4/5, T-SEC-1/2.

### M-09. Usunięcie JWT z backendu

- **Usuń:** `security/JwtAuthenticationConverter.java`, `JwtAuthenticationConverterConfiguration.java`, `JwtConfiguration.java`, `JwtProperties.java`, `SecurityConfiguration.java`, `SecurityCurrentUserProvider.java` (cały katalog `security/` powinien zniknąć po M-07).
- **Usuń:** blok `jwt:` w `src/main/resources/application.yaml` i `src/test/resources/application-test.yaml`.
- **Usuń:** `config/WebConfig.java` (D8, F-9).
- **Weryfikacja:**
  ```bash
  rg -n "jwt|Jwt|oauth2" services/mentorlink-backend/src services/mentorlink-backend/pom.xml   # puste
  ```

### M-10. Testy backendu

- **Infrastruktura testowa (nowe pliki w `src/test/java/com/sniperdev/mentorlink_backend/`):**
  - `TestcontainersConfiguration.java` – `@TestConfiguration(proxyBeanMethods=false)` z `@Bean @ServiceConnection PostgreSQLContainer` (`postgres:17`). **[SPRAWDŹ]** pakiet klasy kontenera w Testcontainers 2.x (`org.testcontainers.postgresql.PostgreSQLContainer`).
  - `identity/AuthTestSupport.java` – helper: wykonuje `GET /api/v1/auth/me` żeby wziąć `XSRF-TOKEN`, zwraca cookies i nagłówek (MockMvc sam nie trzyma cookies).
- **Przepisz:** `src/test/resources/application-test.yaml` → usuń H2 i `jwt`, zostaw tylko ewentualne `spring.session.timeout`. `MentorlinkBackendApplicationTests` → `@SpringBootTest @Import(TestcontainersConfiguration.class)`.
- **Przenieś / zachowaj z auth-service jako wzorzec:** `AuthControllerTest` (przepisz pod nowe ścieżki/odpowiedzi), `AuthServiceTest` (przepisz pod `AuthenticationManager`). **Usuń** bez zastępstwa: `JwtServiceTest`, `JwtAuthenticationFilterIntegrationTest`.
- **Nowe testy:** komplet w §8.
- **Istniejące testy kontrolerów** (`SubjectControllerTest`, `TutorProfileControllerTest`) używają `addFilters = false` – dalej działają, ale **nie** sprawdzają autoryzacji; autoryzację pokrywają nowe testy integracyjne (T-AUTHZ-*).
- **Weryfikacja:** `cd services/mentorlink-backend && ./mvnw -B verify` (wymaga działającego Dockera).

### M-11. Integracja Identity z `tutors` (naprawa F-5)

> Można odroczyć – błąd F-5 istnieje niezależnie od migracji. Zalecane teraz, bo to jedyne miejsce, gdzie moduł `tutors` potrzebuje danych `identity`.

- **Nowe:** `identity/api/UserDirectory.java` (`Map<UUID, UserSummary> findSummaries(Collection<UUID> ids)`), `identity/api/UserSummary.java` (`record(UUID id, String firstName, String lastName)`). Implementacja: `identity/service/UserService` (jedno zapytanie `findAllById`). **Bez e-maila** – nie ujawniamy go publicznie.
- **Zmień:** `tutors/dto/TutorSummaryResponse` → `(UUID id, UUID userId, String firstName, String lastName, String bio)`; `TutorProfileMapper.toSummaryResponse(profile, summary)`; `TutorProfileService.getAllTutors()` pobiera podsumowania hurtowo (unikamy N+1), `getTutorProfile(id)` pojedynczo. Brak podsumowania (konto usunięte) → pominąć profil / `ResourceNotFound`.
- **Frontend (jednocześnie, M-12):** `features/tutors/types/tutor.ts` – usuń `email`; `TutorCard.tsx` – usuń wiersz z `tutor.email`; `(public)/tutors/[id]/page.tsx` – usuń akapit z `tutor.email`.
- **Weryfikacja:** T-INT-1 (lista tutorów zawiera imię/nazwisko, brak `email`), strona główna renderuje karty bez błędów w konsoli.

### M-12. Frontend

Zakres jest ograniczony do tego, co potrzeba, by logowanie/rejestracja/wylogowanie działały z jednym backendem. Nie ruszamy istniejącego UI poza Navbarem.

- **Zmienne środowiskowe:** nic do usunięcia – frontend nigdy nie miał zmiennej wskazującej na `auth-service`. `NEXT_PUBLIC_API_URL` musi być `/api/v1` (rekomendowane w F-6); `BACKEND_INTERNAL_URL` bez zmian. Lokalnie przy `pnpm dev` ustaw `.env.local`: `BACKEND_INTERNAL_URL=http://localhost:8080` (domyślna wartość `http://mentorlink-backend:8080` działa tylko w sieci Dockera).
- **`next.config.ts`:** bez zmian – rewrite `/api/v1/:path*` obejmuje nowe endpointy (D3). **[SPRAWDŹ]** że nagłówki `Set-Cookie` przechodzą przez rewrite (powinny; test ręczny M-17).
- **`src/lib/api/client.ts`:** dodaj `withCredentials: true`. Axios dla tego samego origin sam kopiuje cookie `XSRF-TOKEN` do nagłówka `X-XSRF-TOKEN` (domyślne nazwy) – nie trzeba kodu. Dodaj interceptor odpowiedzi: na `401` dla zapytań innych niż `/auth/*` wywołaj `queryClient.setQueryData(queryKeys.me, null)` (stan „wylogowany”) – bez globalnych przekierowań.
- **Nowe pliki:**
  - `src/features/auth/types/auth.ts` – `CurrentUser { id, email, firstName, lastName, roles: ("STUDENT"|"TUTOR"|"ADMIN")[], status }`.
  - `src/features/auth/api/auth.api.ts` – `register`, `login`, `logout`, `getMe` (+ `index.ts` jak w `features/tutors/api`).
  - `src/features/auth/hooks/useAuth.ts` – `useCurrentUser()` (`useQuery`, klucz `queryKeys.me`, `retry: false`, wynik `null` przy 401), `useLogin()`, `useRegister()`, `useLogout()` (mutacje; po sukcesie `setQueryData(queryKeys.me, …)` / `clear()`).
  - `src/app/(auth)/login/page.tsx`, `src/app/(auth)/register/page.tsx` – proste formularze (kontrolowane `useState` + walidacja `zod`, które już jest w `package.json`); bez nowych bibliotek.
  - `src/components/layout/NavbarAuth.tsx` (`"use client"`) – gdy zalogowany: imię + przycisk „Log out”; gdy nie: linki „Log in” / „Sign up”.
- **Zmień:** `src/lib/react-query/queryKeys.ts` (dodaj `me: ['me'] as const`); `src/components/layout/Navbar.tsx` – podmień dwa martwe przyciski na `<NavbarAuth />` (Navbar pozostaje komponentem serwerowym).
- **Ochrona stron / role:** w repo **nie ma stron wymagających logowania**, więc teraz nic nie chronimy. Gdy pojawi się pierwsza taka strona: użyj `useCurrentUser()` (przekierowanie na `/login`, gdy `null`) i `user.roles.includes("TUTOR")`. Ostateczne decyzje o dostępie zawsze podejmuje backend (401/403). Nie dodawaj `proxy.ts` ani kontekstu ról teraz.
- **Aktywacja Tutora w UI:** poza zakresem (brak strony onboardingu); testowana przez `curl`/DevTools (§9).
- **Błędy:** formularze pokazują `409` („e-mail zajęty”), `401` („nieprawidłowe dane”), `403` („konto zawieszone”), `400` (`validationErrors` z `ApiError`).
- **Weryfikacja:** `pnpm lint && pnpm build` w `apps/mentorlink-frontend`; testy manualne M-17.

### M-13. Docker i Compose

- **`services/mentorlink-backend/Dockerfile`:** bez zmian funkcjonalnych (nadal `EXPOSE 8080`). Opcjonalnie dodaj healthcheck (w JRE jest `curl`):
  `HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=5 CMD curl -fsS http://localhost:8080/actuator/health || exit 1`.
- **`docker-compose.coolify.yml`:**
  - **usuń** cały serwis `auth-service`,
  - w `mentorlink-backend`: usuń `JWT_SECRET`; dodaj `DB_NAME: mentorlink`; dodaj `healthcheck` (`curl` jak wyżej) – opcjonalnie; `ports` zostaw tylko do diagnostyki (`BACKEND_PORT`),
  - w `frontend`: `NEXT_PUBLIC_API_URL: ${FRONTEND_API_URL:-/api/v1}` (F-6), dodaj `BACKEND_INTERNAL_URL` do `args` (`http://mentorlink-backend:8080`), `depends_on` → tylko `mentorlink-backend` (z `condition: service_healthy`, jeśli dodano healthcheck),
  - usuń zmienne: `JWT_SECRET`, `JWT_ACCESS_TOKEN_EXPIRATION`, `JWT_REFRESH_TOKEN_EXPIRATION`, `AUTH_PORT`.
- **`docker-compose.yml`** (tylko Postgres): bez zmian poza dopilnowaniem, że montuje nowy `init.sql` (M-02).
- **Usunięcie obrazu:** po M-18 usuń z rejestru Forgejo pakiet `mentorlink-auth` (UI Forgejo → Packages).
- **Weryfikacja:**
  ```bash
  docker compose -f docker-compose.coolify.yml config -q          # składnia OK
  docker compose -f docker-compose.coolify.yml config --services  # postgres, mentorlink-backend, frontend
  ```

### M-14. Forgejo Actions

- **Usuń:** `.forgejo/workflows/auth.yml`.
- **`.forgejo/workflows/backend.yml`:**
  - krok „Run tests”: dodaj `env: DOCKER_HOST: tcp://docker-in-docker:2375` (i **[SPRAWDŹ]** `TESTCONTAINERS_HOST_OVERRIDE: docker-in-docker`, `TESTCONTAINERS_RYUK_DISABLED: "true"` jeśli Ryuk nie może się połączyć),
  - **plan B**, jeśli Testcontainers na runnerze nie działa: dodaj do joba `services: postgres: image: postgres:17` i podaj testom `SPRING_DATASOURCE_URL` zamiast `TestcontainersConfiguration` – wymagałoby to prostego przełącznika profilu w testach; zdecyduj po M-00.
  - reszta (build, push `forgejo.home.sniperlab.uk/sniperdev/mentorlink-backend`, webhook) bez zmian.
- **`frontend.yml`:** bez zmian (`--build-arg NEXT_PUBLIC_API_URL=/api/v1` już jest poprawne).
- **Secrets w Forgejo:** usuń `COOLIFY_WEBHOOK_URL_AUTH` po M-18.
- **Weryfikacja:** PR z tą gałęzi uruchamia „Backend CI” i „Frontend CI” na zielono; „Auth CI” już nie istnieje.

### M-15. Coolify i reverse proxy

> Zależy od odpowiedzi z M-00 – poniżej dwa warianty.

- **Zasoby w Coolify:** jeden zasób backendu, jeden frontendu, jeden Postgres. Nowy zasób nie jest tworzony – zmieniamy zmienne istniejących.
- **Zmienne backendu:** ustaw `DB_NAME=mentorlink` (lub `DB_URL`), `DB_USERNAME`, `DB_PASSWORD`, **usuń** `JWT_SECRET`. Nie ustawiaj `SESSION_COOKIE_SECURE=false`.
- **Zmienne frontendu:** `NEXT_PUBLIC_API_URL=/api/v1` (build), `BACKEND_INTERNAL_URL` wskazujący nazwę, pod którą backend jest osiągalny z kontenera frontendu.
- **Routing:** domena przypisana **tylko** frontendowi (port 3000). Backend bez publicznej domeny – ruch idzie przez rewrite Next.js. Proxy musi przekazywać `X-Forwarded-Proto/Host` (Traefik/Caddy w Coolify robią to domyślnie).
- **Healthcheck w Coolify:** backend → `GET /actuator/health` (port 8080); frontend → `GET /` (port 3000).
- **Baza:** w Coolify wykonaj `CREATE DATABASE mentorlink OWNER mentorlink;` (M-02, F-8). `auth_db`/`tutor_db` zostaw do M-18.
- **Wariant „trzy osobne Applications z obrazu”:** po zielonym CI na `main` webhook backendu wdroży nowy obraz; kolejność wdrożenia: najpierw backend, potem frontend (frontend ma nowe strony auth, które wołają nowe endpointy). **Uwaga:** webhook uruchamia się dopiero po merge do `main` – nie ma wdrożenia „na próbę”; zalecane jest pełne przetestowanie lokalnie (M-17) przed merge.
- **Wariant „jeden zasób Docker Compose”:** zmień plik compose w zasobie na zaktualizowany `docker-compose.coolify.yml`; usuń zmienne `JWT_*`, `AUTH_PORT`.
- **Weryfikacja:** po wdrożeniu: `curl -fsS https://<domena>/api/v1/tutors` zwraca `200 []`; w DevTools (Application → Cookies) po logowaniu widać `SESSION` (HttpOnly, Secure, SameSite=Lax) i `XSRF-TOKEN` (Secure, bez HttpOnly).

### M-16. Dokumentacja

- **`README.md`:** diagram i opis architektury (jeden backend, sesje), tabela stacku (usuń wiersz „Auth service”/JJWT), sekcja uruchamiania (bez kroku 2 „Start auth-service”, bez wspólnego `JWT_SECRET`), sekcja testów (usuń `cd services/auth-service`; dodaj informację o wymaganym Dockerze), roadmapa (login UI zrobione).
- **`docs/code-map.md`:** zaktualizuj opis uwierzytelniania (sesje zamiast JWT; F-1 przestaje być prawdą) lub oznacz jako nieaktualny do ponownego wygenerowania.
- **`docs/architecture/target-architecture.md` §3.3:** dopisz „zrealizowano w [tym dokumencie]”.
- **Weryfikacja:** `rg -n "auth-service|auth_db|tutor_db|JWT_SECRET" --glob '!docs/architecture/auth-service-migration-plan.md' --glob '!docs/architecture/*discovery*' --glob '!docs/architecture/review/**' .` → tylko świadome wzmianki historyczne.

### M-17. Pełny test systemu przed wyłączeniem `auth-service`

Wykonaj §9 (uruchomienie) i §8 (testy). Dopiero po zielonym wyniku przejdź dalej.

### M-18. Wyłączenie i usunięcie `auth-service`

- **Co:** po akceptacji M-17 (i po wdrożeniu produkcyjnym, jeśli dotyczy):
  1. Coolify: zatrzymaj, potem usuń zasób/serwis `auth-service`; usuń jego domenę/port jeśli istniały.
  2. Forgejo: usuń secret `COOLIFY_WEBHOOK_URL_AUTH`; usuń pakiet kontenera `mentorlink-auth`.
  3. Repo: `git rm -r services/auth-service` oraz (jeśli nie zrobiono) `.forgejo/workflows/auth.yml`.
  4. Baza (po kilku dniach spokoju): `DROP DATABASE auth_db; DROP DATABASE tutor_db;`.
- **Weryfikacja:** `ls services` → tylko `mentorlink-backend`; `docker ps` → brak kontenera auth; pipeline „main” zielony.

---

## 7. Zabezpieczenia: wymagane teraz vs. hardening później

### 7.1. Wymagane w tej migracji (nie wolno pominąć)

| # | Zabezpieczenie | Gdzie |
|---|---|---|
| S-1 | Sesje serwerowe w PostgreSQL, ID sesji w cookie `SESSION`: `HttpOnly`, `Secure`, `SameSite=Lax`, `path=/` | M-05 |
| S-2 | **CSRF włączony** (cookie `XSRF-TOKEN` + nagłówek `X-XSRF-TOKEN`) dla wszystkich metod zmieniających stan, w tym login/register/logout | M-05 |
| S-3 | Zmiana ID sesji przy logowaniu (ochrona przed session fixation) | M-05/M-06 |
| S-4 | Hasła: BCrypt, min. 8 / max. 72 znaki; hash nigdy nie trafia do sesji (`eraseCredentials`) ani do odpowiedzi | M-05/M-06 |
| S-5 | Wylogowanie unieważnia sesję w bazie i kasuje cookie | M-05 |
| S-6 | Wygaśnięcie sesji po bezczynności (TTL 2 h, cleanup co minutę) | M-05 |
| S-7 | Status konta sprawdzany przy logowaniu **i przy każdym żądaniu** (zawieszenie działa natychmiast); `INACTIVE`/`BLOCKED` nie przejdą | M-05/M-06 |
| S-8 | Role czytane z bazy przy każdym żądaniu; autoryzacja: `@PreAuthorize` + własność zasobu z sesji | M-05/M-07 |
| S-9 | Jednolity komunikat błędu logowania, brak ujawniania statusu konta przed sprawdzeniem hasła | M-06 |
| S-10 | Normalizacja e-maila (trim + lower-case) przy rejestracji i logowaniu | M-04/M-06 |
| S-11 | Brak CORS, jeden origin | D8 |
| S-12 | Brak e-maila w publicznym API tutorów | M-11 |
| S-13 | Sekrety poza repozytorium (hasło DB w zmiennych Coolify); `JWT_SECRET` usunięty wszędzie | M-13/M-15 |

### 7.2. Hardening po migracji (świadomie odłożone)

| # | Temat |
|---|---|
| H-1 | Rate limiting / blokada po wielu nieudanych logowaniach (login i register) |
| H-2 | Osobna rola DB bez uprawnień superusera; ewentualnie rola per schemat |
| H-3 | Bezwzględny (absolute) limit życia sesji obok TTL bezczynności |
| H-4 | Nagłówki bezpieczeństwa (HSTS, CSP, `X-Content-Type-Options`, Referrer-Policy) na proxy/Next.js |
| H-5 | Polityka haseł (listy haseł skompromitowanych, siła), weryfikacja e-maila, reset hasła |
| H-6 | Ochrona przed enumeracją kont przy rejestracji (dziś `409`) |
| H-7 | Swagger UI za sesją/CSRF lub wyłączony na produkcji (F-10) |
| H-8 | Audit log logowań i operacji administracyjnych; wylogowanie ze wszystkich urządzeń (`findByPrincipalName`) |
| H-9 | Sezonowe rotowanie sekretów, skan zależności (SCA), logi bez danych osobowych |
| H-10 | Seeder administratora dla dev/test (dziś ADMIN nadawany ręcznie w SQL) |
| H-11 | Pozostała treść Stage 1 z `target-architecture.md` (wiek 18+, strefy czasowe, `GRANT_ADMIN`, JSON-owe 401/403 z `ApiError`) |

---

## 8. Testy, które muszą przejść

### 8.1. Automatyczne (`./mvnw -B verify`, Testcontainers PostgreSQL, prawdziwy Flyway)

| ID | Test | Oczekiwany wynik |
|---|---|---|
| T-CTX-1 | Kontekst Spring wstaje na PostgreSQL; Flyway wykonuje V1–V5 | brak błędów `validate`; schematy `identity`, `tutoring`, `catalog` istnieją |
| T-MIG-1 | `\dt identity.*`, `tutoring.*`, `catalog.*` | oczekiwane tabele, brak `refresh_tokens` |
| T-AUTH-1 | Rejestracja poprawna | `201`, ciało `CurrentUserResponse` z rolą `STUDENT`, `Set-Cookie: SESSION=…; HttpOnly; Secure; SameSite=Lax`, wiersz w `identity.spring_session` |
| T-AUTH-2 | Rejestracja: e-mail zajęty (także różnica wielkości liter) | `409` |
| T-AUTH-3 | Rejestracja: niepoprawne dane (zły e-mail, hasło < 8 i > 72, puste pola) | `400` z `validationErrors` |
| T-AUTH-4 | Login poprawny | `200`, nowy `SESSION` różny od wcześniejszego (fixation) |
| T-AUTH-5 | Login: złe hasło / nieistniejący e-mail | `401`, identyczne ciało w obu przypadkach |
| T-AUTH-6 | Login: konto `BLOCKED` i `INACTIVE` (poprawne hasło) | `403`, brak sesji w bazie |
| T-AUTH-7 | Hash hasła nie występuje w odpowiedziach ani w `attribute_bytes` sesji | brak |
| T-SESS-1 | `GET /auth/me` z sesją | `200`, dane zgodne z bazą |
| T-SESS-2 | `GET /auth/me` bez sesji | `401` |
| T-SESS-3 | Wylogowanie (`POST /auth/logout` z CSRF) | `204`; kolejne `GET /auth/me` tym samym cookie → `401`; wiersz sesji usunięty |
| T-SESS-4 | Wygaśnięcie sesji (ustaw `max_inactive_interval`/`expiry_time` w przeszłości lub krótki TTL) | `401` |
| T-SESS-5 | Konto zablokowane **w trakcie** ważnej sesji (zmiana `status` w DB) | następne żądanie `401`, sesja unieważniona |
| T-SESS-6 | Sesja konta usuniętego z bazy | `401` |
| T-CSRF-1 | `POST /auth/login` / `register` / `logout` / `users/me/roles/tutor` bez nagłówka CSRF | `403` |
| T-CSRF-2 | To samo ze złym tokenem | `403` |
| T-CSRF-3 | Pierwsze `GET /auth/me` (401) | odpowiedź zawiera `Set-Cookie: XSRF-TOKEN=…` (bez `HttpOnly`) |
| T-CSRF-4 | `GET` bez tokenu | nie jest blokowany |
| T-ROLE-1 | `POST /users/me/roles/tutor` | `200`, `roles` zawiera `STUDENT` i `TUTOR`; powtórzone wywołanie – bez zmian |
| T-ROLE-2 | Natychmiastowa widoczność roli: bez ponownego logowania `POST /tutors/profile` | `201` (po aktywacji) |
| T-AUTHZ-1 | `POST /tutors/profile` jako `STUDENT` | `403` |
| T-AUTHZ-2 | `POST /tutors/profile` bez sesji | `401` |
| T-AUTHZ-3 | `POST /subjects` jako `STUDENT`/`TUTOR` | `403`; jako `ADMIN` (rola wstawiona w teście bezpośrednio do `user_roles`) → `201` |
| T-AUTHZ-4 | `GET /subjects`, `GET /tutors`, `GET /tutors/{id}` bez sesji | `200` |
| T-AUTHZ-5 | Dowolny inny endpoint (np. `/api/v1/xyz`) bez sesji | `401` |
| T-AUTHZ-6 | `/actuator/health` bez sesji | `200` |
| T-INT-1 | Zarejestruj → aktywuj Tutora → utwórz profil → `GET /tutors` | element z `firstName`/`lastName`, **bez** `email` |
| T-INT-2 | Drugi `POST /tutors/profile` tego samego użytkownika | `409` (istniejący `ConflictException`) |
| T-INT-3 | `userId` w `TutorProfileResponse` = `id` z sesji (nie z body) | zgodne |
| T-UNIT-1 | `UserService` (normalizacja e-maila, rola domyślna, `addRole` idempotentne), `AuthService` (Mockito), `SessionAccountFilter` (status/role) | zielone |
| T-UNIT-2 | Istniejące testy `SubjectService*`, `TutorProfileService*`, `SubjectControllerTest`, `TutorProfileControllerTest` | zielone po zmianach importów (M-07) |

### 8.2. Frontend

```bash
cd apps/mentorlink-frontend && pnpm install --frozen-lockfile && pnpm lint && pnpm build
```
Brak testów jednostkowych frontendu w repo – nie wprowadzamy ich w tej migracji.

### 8.3. Manualne w przeglądarce (Chrome/Firefox, `http://localhost:18080` po M-17)

1. Wejdź na `/` – strona się ładuje, karty tutorów (jeśli są) bez błędów w konsoli.
2. DevTools → Application → Cookies: po pierwszym żądaniu widoczny `XSRF-TOKEN`.
3. `/register` → poprawne dane → przekierowanie, Navbar pokazuje imię; cookie `SESSION` z flagami HttpOnly, Secure, SameSite=Lax; `document.cookie` **nie** zawiera `SESSION`.
4. Odśwież stronę (F5) → nadal zalogowany. Zamknij i otwórz kartę → nadal zalogowany.
5. „Log out” → Navbar wraca do „Log in/Sign up”; `GET /api/v1/auth/me` → `401`; cookie `SESSION` usunięte.
6. `/login` → złe hasło → komunikat; poprawne → zalogowany.
7. `/register` tym samym e-mailem → komunikat o zajętym adresie.
8. Hasło 7 znaków → komunikat walidacji.
9. Dwie karty: wyloguj w jednej, w drugiej odśwież → wylogowany.
10. Zablokuj konto w SQL (§9.3) przy aktywnej sesji → po odświeżeniu wylogowany.
11. W konsoli przeglądarki: `fetch('/api/v1/auth/logout', {method:'POST'})` bez nagłówka CSRF → `403`.
12. Usuń cookie `XSRF-TOKEN`, spróbuj zalogować z UI → UI musi sobie poradzić (pobranie nowego tokena) lub pokazać zrozumiały błąd.
13. Aktywacja Tutora (DevTools/`curl`, §9.3) → `GET /auth/me` zawiera `TUTOR`.
14. Wykonaj `POST /api/v1/tutors/profile` (jako Tutor) z tokenem CSRF → `201`; lista na `/` pokazuje tutora z imieniem i nazwiskiem.

---

## 9. Procedura uruchomienia całego systemu

### 9.1. Czyste środowisko lokalne

```bash
cp /dev/null .env     # plik poza gitem; wpisz poniższe wartości
cat > .env <<'EOF'
POSTGRES_PASSWORD=change-me-local
EOF

docker compose -f docker-compose.coolify.yml --env-file .env down -v        # czysty start (dane testowe!)
docker compose -f docker-compose.coolify.yml --env-file .env up -d --build
docker compose -f docker-compose.coolify.yml ps
docker compose -f docker-compose.coolify.yml logs -f mentorlink-backend      # szukaj: "Started MentorlinkBackendApplication", migracje Flyway V1..V5
```

Jeśli wolumen Postgresa **nie jest** czyszczony (`down -v` pominięte): najpierw `CREATE DATABASE mentorlink OWNER mentorlink;` (M-02).

### 9.2. Kontrole zdrowia

```bash
curl -fsS http://localhost:18081/actuator/health          # {"status":"UP"}
curl -fsS http://localhost:18080/api/v1/tutors            # 200, [] (przez Next.js)
curl -fsS http://localhost:18080/                         # 200
docker exec mentorlink-postgres-1 psql -U mentorlink -d mentorlink -c '\dt identity.*'   # nazwa kontenera zależy od projektu compose
```

### 9.3. Przepływ ręczny przez `curl` (cookie jar + CSRF)

```bash
B=http://localhost:18080/api/v1
J=/tmp/ml.jar
curl -si -c $J -b $J $B/auth/me | head -5                                   # 401 + Set-Cookie: XSRF-TOKEN
T=$(awk '$6=="XSRF-TOKEN"{print $7}' $J)
curl -si -c $J -b $J -H "X-XSRF-TOKEN: $T" -H 'Content-Type: application/json' \
  -d '{"email":"jan@example.com","password":"password123","firstName":"Jan","lastName":"Kowalski"}' $B/auth/register
curl -si -c $J -b $J $B/auth/me                                              # 200
T=$(awk '$6=="XSRF-TOKEN"{print $7}' $J)
curl -si -c $J -b $J -H "X-XSRF-TOKEN: $T" -X POST $B/users/me/roles/tutor   # 200, roles: STUDENT+TUTOR
curl -si -c $J -b $J -H "X-XSRF-TOKEN: $T" -H 'Content-Type: application/json' -d '{"bio":"Matematyka"}' $B/tutors/profile   # 201
curl -si -c $J -b $J -H "X-XSRF-TOKEN: $T" -X POST $B/auth/logout            # 204

# nadanie ADMIN (jedyny sposób, patrz F-4/H-10) oraz zawieszenie konta – w bazie:
docker exec -i mentorlink-postgres-1 psql -U mentorlink -d mentorlink <<'SQL'
INSERT INTO identity.user_roles (user_id, role)
  SELECT id, 'ADMIN' FROM identity.users WHERE email = 'jan@example.com';
-- zawieszenie: UPDATE identity.users SET status = 'BLOCKED' WHERE email = 'jan@example.com';
SQL
```
(Uwaga: `curl` po HTTP zapisze cookie `Secure` w jar, ale `curl -b` wyśle je też po HTTP – to zachowanie narzędzia, nie przeglądarki.)

### 9.4. Zamknięcie

Jeśli §8 jest zielone i §9.2–9.3 działa: merge do `main` → pipeline buduje obrazy i wywołuje webhooki (M-15) → powtórz §9.2/§9.3 na środowisku docelowym → M-18.

---

## 10. Rollback

Migracja nie modyfikuje niczego, co zepsułoby poprzedni stan: `auth-service`, jego obraz, `auth_db` i `tutor_db` zostają do M-18.

| Moment niepowodzenia | Postępowanie |
|---|---|
| Przed merge do `main` (lokalnie, M-01…M-17) | Porzuć gałąź (`git checkout main && git branch -D migration/auth-into-backend`). `docker compose -f docker-compose.coolify.yml down -v`. Nic w środowisku docelowym nie zostało zmienione. |
| Po merge, nowe wdrożenie nie startuje / testy dymne z §9.2–9.3 nie przechodzą | 1) W Coolify wdróż ponownie poprzedni obraz backendu i frontendu (tagi `:<sha>` poprzedniego commita są w rejestrze Forgejo – CI taguje każdy commit) albo „Redeploy” poprzedniej wersji. 2) Przywróć zmienne środowiskowe sprzed zmian (`JWT_SECRET`, `DB_NAME=tutor_db` dla backendu) – dlatego **nie usuwaj** ich z Coolify przed M-18, tylko zapisz poprzednie wartości. 3) Upewnij się, że `auth-service` nadal działa (nie został usunięty). 4) `git revert -m 1 <merge-commit>` na `main` (przywraca kod, workflow `auth.yml` i `docker-compose.coolify.yml`). |
| Baza `mentorlink` jest w złym stanie (np. nieudane migracje Flyway) | Usuń i utwórz od nowa: `DROP DATABASE mentorlink; CREATE DATABASE mentorlink OWNER mentorlink;` – zawiera tylko dane testowe. Stare bazy `auth_db`/`tutor_db` nie były dotykane. |
| Po M-18 (auth-service usunięty) | Rollback = `git revert` + odtworzenie zasobu `auth-service` w Coolify z obrazu `mentorlink-auth:<sha>` (jeśli pakiet nie został skasowany) lub z przebudowanego commita sprzed usunięcia + odtworzenie `auth_db` (`CREATE DATABASE`; Flyway utworzy tabele). Dlatego M-18 wykonuj dopiero po okresie obserwacji. |

---

## 11. Lista kontrolna końcowa

**Kod i konfiguracja**
- [ ] `services/auth-service` usunięty z repozytorium; `.forgejo/workflows/auth.yml` usunięty.
- [ ] `rg -n "jwt|Jwt|oauth2|jjwt" services/ docker-compose*.yml .forgejo` – brak wyników.
- [ ] `services/mentorlink-backend/src/main/java/.../security/` nie istnieje; `identity/` istnieje wg §4.
- [ ] `config/WebConfig.java` usunięty.
- [ ] Migracje V1–V5 działają na pustej bazie; `ddl-auto: validate` przechodzi.
- [ ] `application.yaml`: `DB_NAME` domyślnie `mentorlink`, ustawienia sesji i cookie wg M-05, brak `jwt:`.
- [ ] `pom.xml`: dodane `spring-boot-starter-session-jdbc` i Testcontainers; usunięte `oauth2-resource-server`, `h2`.

**Testy**
- [ ] `./mvnw -B verify` (backend) zielone na PostgreSQL (Testcontainers) – wszystkie testy z §8.1.
- [ ] `pnpm lint && pnpm build` (frontend) zielone.
- [ ] Testy manualne §8.3 (14 punktów) wykonane.

**Infrastruktura**
- [ ] `docker-compose.coolify.yml` bez `auth-service`, bez `JWT_*`, `NEXT_PUBLIC_API_URL=/api/v1`.
- [ ] `docker/postgres/init/init.sql` tworzy `mentorlink`.
- [ ] CI: „Backend CI” i „Frontend CI” zielone na PR; brak „Auth CI”.
- [ ] Coolify: działa jedna aplikacja backend + jedna frontend + Postgres; domena tylko na frontendzie.
- [ ] Zmienne `JWT_*`, `AUTH_PORT`, `COOLIFY_WEBHOOK_URL_AUTH` usunięte (po M-18).
- [ ] `auth_db`, `tutor_db` usunięte (po okresie obserwacji).

**Bezpieczeństwo (§7.1)**
- [ ] S-1…S-13 zweryfikowane testami T-AUTH/T-SESS/T-CSRF/T-ROLE/T-AUTHZ.

**Dokumentacja**
- [ ] `README.md`, `docs/code-map.md` zaktualizowane (M-16).

---

## 12. Założenia i otwarte kwestie

| # | Kwestia | Status |
|---|---|---|
| O-1 | Dokładne nazwy artefaktów/pakietów w Boot 4.0.7 / Spring Security 7 / Testcontainers 2 (`csrf.spa()`, `PostgreSQLContainer`, schemat `spring_session`) | **[SPRAWDŹ]** przy M-03/M-05/M-10 – jeśli coś różni się od planu, zadanie wskazuje alternatywę |
| O-2 | Konfiguracja Coolify (jeden compose czy trzy aplikacje), domeny, TLS na LAN | Do ustalenia w M-00 |
| O-3 | Czy runner Forgejo pozwala Testcontainers na dostęp do Dockera | Do sprawdzenia w M-14; plan B opisany |
| O-4 | Czy nagłówki `Set-Cookie` przechodzą przez rewrite Next.js bez zmian | Do sprawdzenia w M-17 (spodziewane: tak) |
| O-5 | Czy `server.servlet.session.timeout` czy `spring.session.timeout` steruje TTL sesji JDBC w tej wersji | Do sprawdzenia w T-SESS-4 |
| O-6 | M-11 (naprawa F-5) – wykonać w tej migracji czy odroczyć | Rekomendacja: wykonać, bo to jedyna integracja `identity` ↔ `tutors` |
| O-7 | Brak środowiska Java/Maven w miejscu tworzenia planu | Nic z tego dokumentu nie było kompilowane ani uruchamiane |
