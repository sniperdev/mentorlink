# MentorLink – Product Vision

> **Status:** product discovery completed on 2026-11-12. Sections 1–11 describe the long-term vision. Sections 12–17 record the agreed MVP requirements, engineering principles, assumptions, future readiness and decisions deferred to ADRs. This document contains **no technical or architectural decisions**.

## 1. Product Overview

MentorLink is an end-to-end mentoring and online education platform designed to connect students with tutors and mentors.

The platform aims to provide a complete environment for discovering educators, booking lessons, managing schedules, making payments, communicating, sharing educational materials and conducting online sessions.

Rather than being just a tutor directory or marketplace, MentorLink is intended to support the entire educational journey within a single platform.

## 2. Project Goals and Context

MentorLink is currently a personal software engineering and portfolio project developed independently alongside the developer's professional work.

The primary objectives are:

- Demonstrate advanced software engineering and architectural skills.
- Build a realistic application with business complexity comparable to commercial SaaS platforms.
- Explore modern architectural patterns and technologies.
- Practice designing, implementing and maintaining distributed systems.
- Develop the application incrementally over an extended period.

The project is not currently intended for commercial launch or immediate monetization.

There is no requirement to optimize for rapid market entry or support a large production user base from the beginning.

However, architectural decisions should demonstrate professional engineering standards, maintainability, security and scalability.

Future commercial possibilities should not be excluded, but they are not current business requirements.

## 3. Product Vision

MentorLink should become a comprehensive mentoring ecosystem connecting students and educators.

A student should be able to:

1. Discover tutors based on subjects, skills and specializations.
2. Review tutor profiles, experience, services and pricing.
3. Select a suitable tutor.
4. Browse the tutor's availability calendar.
5. Book an available lesson.
6. Pay for the lesson through the platform.
7. Communicate directly with the tutor.
8. Exchange files and educational materials.
9. Attend online lessons through integrated video conferencing.
10. Access information about upcoming and previous lessons.

A tutor should be able to:

1. Create and manage a professional profile.
2. Define subjects and specializations.
3. Configure services and pricing.
4. Manage availability and working hours.
5. Accept and manage bookings.
6. Communicate with students.
7. Share educational materials and files.
8. Conduct online lessons.
9. Manage their teaching activity.

## 4. Core Product Domains

The following are preliminary business capability areas, not predefined microservice boundaries.

### 4.1 Identity and Access Management

- User registration and authentication.
- Student, tutor and administrator roles.
- Authorization and permissions.
- Account management.
- Profile identity information.
- Account lifecycle and security.

### 4.2 Tutor Marketplace

- Tutor discovery and search.
- Tutor profiles.
- Subject and specialization catalog.
- Services and pricing.
- Reviews and ratings.
- Tutor visibility and availability status.

### 4.3 Scheduling and Booking

- Tutor availability management.
- Calendar management.
- Lesson scheduling.
- Booking creation and cancellation.
- Booking lifecycle and status management.
- Conflict prevention and concurrent booking handling.
- Time zones and scheduling rules.

### 4.4 Payments and Billing

- Online lesson payments.
- Payment status management.
- Transaction history.
- Tutor settlements.
- Refunds and cancellations.
- Potential packages and subscriptions.

### 4.5 Communication

- Private student-tutor conversations.
- Real-time messaging.
- Conversation history.
- Notifications.
- File sharing within conversations.

### 4.6 File and Material Management

- Private file storage.
- Educational materials.
- Assignment and solution sharing.
- File access permissions.
- Student-tutor shared workspaces.
- File lifecycle and storage management.

The intended experience should resemble a private collaborative workspace, similar in concept to a shared Google Drive environment, but integrated into the mentoring platform.

### 4.7 Online Lessons

- Integrated video conferencing.
- Lesson sessions associated with bookings.
- Secure session access.
- Lesson lifecycle management.
- Potential lesson recordings, subject to future requirements.

### 4.8 Administration and Platform Management

- User management.
- Tutor management and moderation.
- Subject catalog management.
- Platform configuration.
- Administrative reporting.
- Audit and moderation capabilities.

## 5. Future Product Capabilities

The following capabilities are potential long-term extensions, not immediate implementation requirements.

### 5.1 Online Courses

- Self-paced courses.
- Educational content and learning materials.
- Course enrollment.
- Learning progress tracking.
- Course access management.

### 5.2 Group Lessons

- Multiple students participating in one session.
- Group scheduling.
- Group bookings.
- Group payment models.
- Group communication.

### 5.3 Subscriptions and Packages

- Recurring subscriptions.
- Lesson packages.
- Membership plans.
- Different pricing models.

### 5.4 Advanced Platform Features

- Advanced analytics.
- Personalized tutor recommendations.
- Automated workflows.
- Advanced notifications.
- Potential AI-assisted educational capabilities.

These ideas should not drive premature architectural complexity.

## 6. User Types

The currently envisioned user categories are:

- Student: discovers tutors, books lessons, makes payments and accesses educational services.
- Tutor: offers educational services, manages availability and conducts lessons.
- Administrator: manages platform users, content and operations.

One account may hold the Student and Tutor roles at the same time (see §12.8). Holding the Tutor role does not by itself publish a tutor profile.

Additional roles may be introduced if future requirements justify them.

## 7. Non-Functional Expectations

The architecture should consider:

- Maintainability and clean separation of responsibilities.
- Strong authentication and authorization.
- Secure handling of personal data.
- Secure file access and storage.
- Reliable booking and payment workflows.
- Consistent data ownership.
- Appropriate API contracts.
- Testability and automated quality checks.
- Observability and operational visibility.
- Independent deployment where justified.
- Extensibility for future business capabilities.

The system should be designed to evolve without requiring unnecessary rewrites.

## 8. Engineering and Technology Philosophy

MentorLink is also an engineering portfolio project.

The architecture should demonstrate realistic enterprise software engineering practices, including:

- Domain-Driven Design where appropriate.
- Well-defined bounded contexts.
- Modular architecture.
- Microservices when justified by domain or operational boundaries.
- REST APIs and asynchronous communication where appropriate.
- Explicit data ownership.
- Architecture Decision Records.
- Automated testing and CI/CD.
- Infrastructure as Code where appropriate.
- Monitoring, logging and distributed tracing.

Technology choices must be justified by actual requirements and trade-offs.

Do not introduce infrastructure or architectural patterns solely to make the project appear more sophisticated.

## 9. Development Constraints

- The project is developed independently during personal time.
- Development is incremental and long-term.
- There is no fixed commercial launch deadline.
- Operational and infrastructure complexity should remain manageable for one developer.
- The current implementation should be evolved where reasonable rather than rewritten without justification.

## 10. Architectural Decision Principles

When designing the target architecture:

1. Start with business domains and responsibilities, not the number of microservices.
2. Distinguish current requirements from future possibilities.
3. Avoid premature implementation of speculative features.
4. Prefer clear domain boundaries and explicit contracts.
5. Evaluate modular monolith and microservice alternatives objectively.
6. Introduce distributed infrastructure only when justified.
7. Consider the operational burden of every additional component.
8. Preserve a practical migration path from the current implementation.
9. Document significant decisions and alternatives.
10. Design for long-term evolution and professional engineering quality.

## 11. Expected Outcome

The desired outcome is a well-structured, extensible mentoring platform that can progressively evolve from its current early-stage implementation into a comprehensive online education ecosystem.

The architecture should support this evolution while remaining realistic to build and maintain as a solo developer.

---

## 12. MVP Requirements (agreed in product discovery)

> This section records the **business requirements** agreed in product discovery. It deliberately contains **no technical or architectural decisions**: storage formats, libraries, payment integration mechanics, idempotency mechanisms and service boundaries are decided during architecture design (§16).

Requirement IDs (`BR-…`) are stable references for later ADRs and tests.

### 12.1 MVP scope

- **BR-SCOPE-1** The MVP covers one end-to-end journey: a student discovers a tutor, books a lesson, pays for it and gains access to the meeting.
- **BR-SCOPE-2** Payments use a real payment provider in **test mode** (e.g. Stripe Test Mode). No real money moves.
- **BR-SCOPE-3** There are no real settlements or payouts to tutors in the MVP. The payment model must allow payouts and settlements to be added later without redesigning the booking process.
- **BR-SCOPE-4** Meeting access is provided through a per-booking link to a lesson page (§12.9). The MVP includes no platform-operated video infrastructure and no integrated external video provider.
- **BR-SCOPE-5** The highest priority is **correctness of the booking process**: availability, temporary holds, double-booking prevention, payment outcome handling, cancellation and release of time.

### 12.2 Booking model

- **BR-BOOK-1** Booking is **instant**. The student selects an available start time, the system places a temporary **hold**, and a successful payment confirms the booking automatically, with no tutor approval.
- **BR-BOOK-2** Declared tutor availability is a commitment: the tutor does not approve individual lessons.
- **BR-BOOK-3** If payment is not completed while the hold is active, the hold expires and the time returns to availability.
- **BR-BOOK-4** These edge cases must be handled explicitly: two students trying to book the same time; a payment confirmed after the hold expired; the provider re-sending the same notification; a payment that fails or whose outcome is unknown; a student abandoning the payment.

### 12.3 Services, availability and conflicts

- **BR-AVAIL-1** A tutor defines **services**, each with a name, description, duration and price.
- **BR-AVAIL-2** A student selects a service. The system computes the possible start times from the tutor's weekly schedule, schedule exceptions, existing bookings and active holds.
- **BR-AVAIL-3** Conflicts are detected by **overlapping time intervals**, not by identifiers of predefined slots.
- **BR-AVAIL-4** **Buffer:** one value per tutor, default 0 minutes. It is the minimum gap between the end of one lesson and the start of the next (with a 15-minute buffer, a lesson ending at 11:00 allows the next one at 11:15 at the earliest). Buffers do not add up.
- **BR-AVAIL-5** **Minimum notice:** a booking must start at least **12 hours** after the hold is created.
- **BR-AVAIL-6** **Booking horizon:** at most **8 weeks** ahead.
- **BR-AVAIL-7** Service limits: duration **15–240 minutes in 15-minute steps**; price **1–2000 PLN**. Free services are not part of the MVP.

### 12.4 Time zones and daylight saving time

- **BR-TZ-1** The tutor sets their time zone in the profile. Changing it later is not a separate MVP business process.
- **BR-TZ-2** The tutor's schedule is defined in the tutor's **local** time. "Monday 16:00" stays 16:00 local time across DST changes.
- **BR-TZ-3** The student's time zone defaults to the one detected by the browser and can be overridden in profile settings.
- **BR-TZ-4** A booking represents a **fixed point in time**. Later changes to either user's time-zone settings never change when a booked lesson happens.
- **BR-TZ-5** Lesson duration always means **real elapsed time**, including across a DST change.
- **BR-TZ-6** **Non-existent local times** (spring-forward gap): that part of the tutor's window does not exist on that day, and the window is not shifted.
- **BR-TZ-7** **Repeated local times** (fall-back overlap): the window lasts the real elapsed time, and the two occurrences are presented as **two separate start options**.
- **BR-TZ-8** When the student and tutor change clocks on different dates, the student always sees the time converted for the specific date. No fixed offset between users is assumed.
- **BR-TZ-9** All views and notifications show times in the **recipient's** zone with the zone abbreviation, plus the UTC offset where needed to avoid ambiguity.
- **BR-TZ-10** The rules apply to **any supported time zone**, not only Europe/Warsaw.

### 12.5 Holds, payments and payment outcomes

- **BR-PAY-1** A hold lasts **30 minutes**. While active it occupies the time **including the tutor's buffer**, exactly like a confirmed booking.
- **BR-PAY-2** A student may have at most **one active hold** across the whole platform.
- **BR-PAY-3** A student may release their own hold early (cancel button). The time returns to availability immediately.
- **BR-PAY-4** Payment methods in the MVP: **card and BLIK**. Przelewy24 is a later option.
- **BR-PAY-5** A failed payment may be **retried within the same hold**. A retry does not reset or extend the 30 minutes.
- **BR-PAY-6** **Late payment:** a payment confirmed after its hold expired confirms the booking only if the time is **still available and satisfies all business rules**, including the 12-hour notice re-checked at that moment and the tutor's profile still being bookable. Otherwise the student receives an **automatic full refund** and a notification.
- **BR-PAY-7** The confirm-or-refund decision reflects current availability and must be **unambiguous**, even when another student is booking the same time concurrently.
- **BR-PAY-8** **Unknown outcome:** if the payment is still being verified when the hold expires, the hold expires normally. The eventual result is handled under BR-PAY-6. Meanwhile the student sees the status "payment being verified" and is notified of the result.
- **BR-PAY-9** **Duplicate provider notifications** never create more than one booking or more than one refund.
- **BR-PAY-10** One hold results in **at most one paid booking**. If several payment attempts within one hold succeed, the student is not charged more than once: surplus successful payments are **fully refunded**.

### 12.6 Pricing, currency and immutability of booked terms

- **BR-PRICE-1** One currency in the MVP: **PLN**. Prices are **gross**, the final amount the student pays.
- **BR-PRICE-2** Every amount is recorded together with its currency, so that multiple currencies can be supported later.
- **BR-PRICE-3** Taxes and real tutor settlements are outside the MVP.
- **BR-PRICE-4** A hold and the resulting booking keep a **snapshot of the service terms at hold creation**: name, duration, price and currency. The student pays the amount accepted when the hold was created.
- **BR-PRICE-5** Changing or withdrawing a service never changes existing bookings or the terms of an active hold.
- **BR-PRICE-6** A service can be **withdrawn** from the offer. Its history cannot be deleted while bookings reference it.
- **BR-PRICE-7** Changes to working hours, exceptions or buffer affect **future availability only**. Existing bookings and active holds remain valid.
- **BR-PRICE-8** When a schedule change conflicts with existing bookings, the system **warns the tutor and lists the affected bookings**. It never cancels them automatically. A tutor who cannot hold a lesson must cancel it explicitly (BR-CANCEL-3).

### 12.7 Cancellation, refunds and lesson status

- **BR-CANCEL-1** A student may cancel **at least 24 hours** before the lesson starts and receive a **full refund**.
- **BR-CANCEL-2** A student may cancel **less than 24 hours** before the start, but **without a refund**. The 24-hour threshold is fixed platform-wide in the MVP.
- **BR-CANCEL-3** A tutor may cancel **at any time** before the lesson. The student always receives a **full refund** and a notification. There are no tutor penalties in the MVP.
- **BR-CANCEL-4** After a cancellation, the time returns to availability if it still satisfies the schedule and minimum-notice rules.
- **BR-CANCEL-5** There is no separate reschedule operation in the MVP: the student cancels and books again. An atomic reschedule that keeps the payment is a later extension.
- **BR-CANCEL-6** The MVP supports **full refunds only**. The model must allow **partial refunds** later.
- **BR-CANCEL-7** No-shows are **not resolved automatically**. After the scheduled end, a lesson gets the neutral status **"completed"**, not "attended", because the system has no confirmation that both parties took part. Disputes are a later concern.

### 12.8 Roles, tutor profiles and moderation

- **BR-ID-1** One account may hold the **Student and Tutor** roles at the same time. The user has one login and switches context.
- **BR-ID-2** Any user can **activate the Tutor role** themselves.
- **BR-ID-3** **Role and tutor profile status are separate concepts.** Holding the Tutor role does not publish a profile.
- **BR-ID-4** A complete tutor profile requires a bio, at least one service, a weekly schedule and a time zone.
- **BR-ID-5** A tutor is **visible in search and bookable** only when the profile is complete, has passed moderation, and is active (not hidden or suspended).
- **BR-ID-6** Administrator accounts are created only through the dev/test provisioning mechanism or granted by a suitably privileged administrator (BR-ADM-4). Public registration never grants the Administrator role.
- **BR-ID-7** An administrator can manually **hide or suspend** a tutor profile and suspend users.
- **BR-ID-8** Suspending or hiding a profile **does not automatically cancel** existing bookings. Cancelling them requires an explicit decision and gives the affected students a full refund.
- **BR-ID-9** **Self-pause:** a tutor may hide their own profile and publish it again themselves. A hidden profile accepts no new bookings, and existing bookings remain valid.
- **BR-ID-10** **Suspension by an administrator:**
  - Active holds on the tutor's time expire normally.
  - A payment arriving later ends in a **full refund**, because the profile is suspended (BR-PAY-6).
  - Only an administrator can lift a suspension.
- **BR-ID-11** A tutor **cannot book their own services**.
- **BR-ID-12** **Content policy:** a profile must not contain contact details (phone, e-mail, messengers) or invitations to pay outside the platform (payment links). In the MVP this is a **policy rule only**, with no automatic enforcement. It is enforced later by real moderation or by an administrator.

**Profile state dimensions**

- **BR-ID-13** Three dimensions of a tutor profile are tracked **independently**:
  1. **Visibility**: whether the profile appears in search and accepts bookings. It is determined by completeness, self-pause (BR-ID-9) and administrative hide or suspension (BR-ID-7, BR-ID-10).
  2. **Published version**: the profile content that students currently see.
  3. **Moderation status of a new version**: whether a submitted revision is pending, finished, or there is none.

  Example: a profile can be active and visible, showing its published version, while a new version is pending moderation.

**Moderation (MVP: simulated)**

- **BR-MOD-1** The tutor creates or updates the profile and **submits it for moderation**. The profile enters the status **"pending moderation"**.
- **BR-MOD-2** In the MVP moderation is **simulated**: after a short simulated processing time (a few seconds) it always ends with a **positive** result.
- **BR-MOD-3** After a positive result the profile is published if it meets the completeness rules (BR-ID-4), and the tutor is **notified** that the process finished.
- **BR-MOD-4** **Profile status and the moderation process are kept separate**, so the simulation can later be replaced by real moderation (for example LLM-assisted) without rebuilding onboarding.
- **BR-MOD-5** A positive moderation result is **never** a confirmation of the tutor's competence.
- **BR-MOD-6** Three things are kept separate: (1) moderation of profile content, (2) student ratings and reviews based on real lessons, (3) verification of tutor qualifications, which is a possible future feature.
- **BR-MOD-7** **Updating a published profile:** the previously published version **stays visible** while the new version is being moderated. Only when moderation finishes positively does the new version **replace** the published one. A pending revision never hides an otherwise visible profile.

### 12.9 Meeting access

- **BR-MEET-1** Every **confirmed** booking is automatically assigned a **unique meeting link**. Links are never shared between lessons, including lessons of the same tutor. The tutor does not add links manually.
- **BR-MEET-2** Only the **student and tutor of that confirmed booking** can see the link. An active hold does not grant access. Administrators have no access to meeting links in the MVP.
- **BR-MEET-3** The link is available **from the moment the booking is confirmed**. After cancellation it is no longer visible to either party.
- **BR-MEET-4** **Accepted MVP limitations:** the platform does not control onward sharing of the link and does not verify who actually attended. Lessons keep the neutral "completed" status after their scheduled end (BR-CANCEL-7).
- **BR-MEET-5** **Assigning a unique link** and **providing a working video meeting** are separate concerns. Generating an address does not by itself imply a working video room.
- **BR-MEET-6** The unique link leads to a **lesson page on the platform**, which **checks access on every visit** (BR-MEET-2). Cancellation therefore really revokes access, not just hides the link.
- **BR-MEET-7** In the MVP the video part of the lesson page is **explicitly marked as a demonstration**. Connecting a real external video provider behind the same lesson page is a later extension and must not change existing links or access rules.

### 12.10 Tutor discovery

- **BR-DISC-1** Students can filter tutors by **subject** (from the subject catalogue) and by **price range**, search by text in the tutor's **name and bio**, sort by **price**, and page through results.
- **BR-DISC-2** Subjects come from a **catalogue managed by administrators**. Tutors assign their services to catalogue subjects.
- **BR-DISC-3** Only visible tutors appear in results (BR-ID-5), showing their published version (BR-MOD-7).
- **BR-DISC-4** Filtering by availability on a specific date or time is **outside the MVP**.

### 12.11 Reviews (outside the MVP, rule recorded)

- **BR-REV-1** Ratings and reviews are **not part of the MVP**.
- **BR-REV-2** Future rule: a student may leave **one review per booking** that reached the status "completed".
- **BR-REV-3** A review does **not** imply verified attendance (BR-CANCEL-7, BR-MEET-4) and is kept separate from profile moderation and qualification verification (BR-MOD-6).

### 12.12 Notifications

- **BR-NOTIF-1** Channels: **in-app** and **e-mail**.
- **BR-NOTIF-2** Events:
  - booking confirmed (both parties);
  - payment failed;
  - payment result after verification;
  - refund issued;
  - cancellation by the student or by the tutor (the other party is notified);
  - reminder 24 h before the lesson;
  - profile moderation finished;
  - profile hidden or suspended by an administrator.
- **BR-NOTIF-3** In test environments all e-mails are **captured by a test mailbox** and never reach real recipients.
- **BR-NOTIF-4** Notifications **never block or roll back** a business operation. A failed notification does not affect the booking, payment or refund it reports.

### 12.13 Administration

- **BR-ADM-1** MVP administrative scope:
  - manage the subject catalogue;
  - hide or suspend tutor profiles, and suspend users;
  - view a user's bookings, payment statuses, amounts and refund history;
  - cancel a booking with a full refund (e.g. for a suspended tutor, BR-ID-8);
  - grant the Administrator role.
- **BR-ADM-2** Administrators have **no access to sensitive payment-instrument data**, such as card details.
- **BR-ADM-3** **Every administrative operation is audited:** who, what, when and why (a reason is mandatory).
- **BR-ADM-4** Granting the Administrator role is limited to **suitably privileged administrators**. Not every administrator may grant it. How such privileges are modelled is decided in an ADR.
- **BR-ADM-5** No reporting or analytics in the MVP.

### 12.14 Personal data and privacy

- **BR-PRIV-1** A user can **request deletion of their own account**. Deletion is allowed only when the user has no active hold, no future confirmed bookings (as student or tutor), and no unresolved payments or refunds. Pending bookings must first be cancelled under §12.7.
- **BR-PRIV-2** On deletion, **identifying data** (name, e-mail, bio, profile) is deleted or anonymised. Booking, payment, refund and audit history is **kept in anonymised form**.
- **BR-PRIV-3** Account deletion must **never make transaction history or the other party's data inconsistent**.
- **BR-PRIV-4** Students and tutors see **only each other's display names**, never e-mail addresses.
- **BR-PRIV-5** Users must **confirm they are at least 18 years old** at registration. Minors with guardian consent are outside the MVP.
- **BR-PRIV-6** Data export (right of access) is a **future requirement**, outside the MVP. Automatic retention and deletion of historical data is outside the MVP; retention periods are decided later.
- **BR-PRIV-7** MVP environments contain **test and demonstration data only**. Security controls are nevertheless designed **as if real personal data were processed**.

### 12.15 Environments

- **BR-ENV-1** Test payments must work in the MVP environment **without exposing the whole application publicly**. The mechanism for receiving payment-provider events is an ADR decision.
- **BR-ENV-2** A public demo is **not part of the MVP**. Architecture and environment configuration must not preclude a later public deployment with TLS and appropriate security controls.


## 13. Engineering Goals, Quality Priorities and Constraints

These principles were agreed in discovery (closing Q-13). They are used to resolve trade-offs during architecture design.

- **EC-1** **Maintainability by one developer takes precedence** over demonstrating distributed systems.
  - Distributed patterns (separate services, asynchronous communication, sagas) are introduced only where business requirements justify them, for example asynchronous payment outcomes (BR-PAY-8) or non-blocking notifications (BR-NOTIF-4).
  - Portfolio value comes from the quality of domain boundaries, contracts, ADRs, tests and the correctness of the solution, not from the number of services.
- **EC-2** **The current split into auth-service and backend does not determine the target architecture.** It may be kept, merged or rebuilt based on analysis. Evolution is preferred over rewriting without justification (§9).
- **EC-3** **Quality priorities**, in order, for resolving trade-offs:
  1. Correctness of booking and payments
  2. Security and data protection
  3. Maintainability and testability
  4. Observability
  5. Evolvability
  6. Scalability and performance
- **EC-4** **Operational constraints:**
  - the whole system runs on **one host**;
  - it uses the existing self-hosted **Coolify and Forgejo** environment;
  - **zero budget** for external services in the MVP (test modes, free tiers);
  - every additional infrastructure component requires justification in an ADR.
- **EC-5** **Readiness levels** for future capabilities (applied in §15):
  - **A — Do not preclude:** the architecture must leave room for the capability, but it is not implemented now.
  - **B — Consider in domain boundaries:** taken into account when drawing domain boundaries, without dedicated technical provisions.
  - **C — Consciously not considered:** a later rebuild is accepted. In particular, the booking model stays **1:1** (one student per lesson).

## 14. Assumptions

- **A-1** For the MVP the deployment remains a local/LAN development and testing environment, not intended for public use (BR-ENV-2).
- **A-2** The payment provider operates in test mode, so no regulatory, tax or payout obligations apply to the MVP (BR-SCOPE-2, BR-PRICE-3).
- **A-3** In the MVP the meeting is a demonstration lesson page with access control. No external video provider is integrated (BR-MEET-6, BR-MEET-7).
- **A-4** These values are preliminary MVP defaults and may become configurable later: the 12-hour minimum notice, the 8-week horizon, the 30-minute hold, the 24-hour cancellation threshold and the service limits.
- **A-5** MVP environments hold test and demonstration data only, but are secured as if they processed real personal data (BR-PRIV-7).

## 15. Future Capabilities and Readiness

Everything here is **outside the MVP**. "Level" refers to EC-5. "—" means discovery did not assign a readiness level; such items are treated as ordinary future work with no architectural provision.

| Capability | Level | Reference |
|---|---|---|
| Real payments, tutor payouts and settlements | A | BR-SCOPE-3 |
| Partial refunds | A | BR-CANCEL-6 |
| Multiple currencies | A | BR-PRICE-2 |
| Real profile moderation (e.g. LLM-assisted) | A | BR-MOD-4 |
| Working video behind the lesson page (external provider) | A | BR-MEET-7 |
| Atomic reschedule keeping the payment | A | BR-CANCEL-5 |
| Optional tutor approval of bookings | A | §12.2 |
| Ratings and reviews | A | BR-REV-* |
| Personal data export | A | BR-PRIV-6 |
| Public demo deployment | A | BR-ENV-2 |
| Student–tutor chat and real-time messaging | B | §4.5 |
| Shared files and educational materials | B | §4.6 |
| Group lessons | C | §5.2, EC-5 |
| Online courses | C | §5.1 |
| Subscriptions and lesson packages | C | §5.3 |
| AI capabilities beyond profile moderation | C | §5.4 |
| Minors with guardian consent | C | BR-PRIV-5 |
| Taxes | — | BR-PRICE-3 |
| Przelewy24 and other payment methods | — | BR-PAY-4 |
| Cancellation fees and tutor penalties | — | BR-CANCEL-2, BR-CANCEL-3 |
| Free services or trial lessons | — | BR-AVAIL-7 |
| Manual moderation queue and appeals | — | BR-MOD-2 |
| Tutor qualification verification | — | BR-MOD-6 |
| Platform-operated video conferencing, recordings | — | §4.7 |
| Search by availability on a date or time | — | BR-DISC-4 |
| Admin reporting and analytics | — | BR-ADM-5 |
| Automatic data retention | — | BR-PRIV-6 |
| Tutor time-zone change as a business process | — | BR-TZ-1 |
| Automatic no-show handling and disputes | — | BR-CANCEL-7 |

Chat and shared files (level B) are expected to use the same access principle as the lesson page: **access derives from a shared booking relationship** (BR-MEET-2).

## 16. Decisions Deferred to Architecture Design (ADR Candidates)

Discovery deliberately left these decisions open. Each is to be decided in an ADR against the requirements referenced.

| # | Decision | Driven by |
|---|---|---|
| D-1 | Domain and service boundaries: modular monolith vs services; the future of the current auth-service/backend split | EC-1, EC-2, EC-4, §4 |
| D-2 | Representation and storage of dates, times and time zones, including DST handling | BR-TZ-* |
| D-3 | Concurrency control for holds, conflict detection and the confirm-or-refund decision; the hold-expiry mechanism | BR-AVAIL-3, BR-PAY-1, BR-PAY-6, BR-PAY-7 |
| D-4 | Payment provider integration and handling of duplicate or out-of-order provider events | BR-PAY-*, BR-SCOPE-3, BR-CANCEL-6 |
| D-5 | Receiving payment-provider events in a non-public environment | BR-ENV-1 |
| D-6 | Notification delivery mechanism and the test mailbox | BR-NOTIF-* |
| D-7 | How the moderation process is decoupled from profile status, so the simulation can be replaced | BR-MOD-4, BR-ID-13 |
| D-8 | Model for administrator privileges (who may grant the Administrator role) | BR-ADM-4, BR-ID-6 |
| D-9 | Lesson page link format and per-visit access check | BR-MEET-1, BR-MEET-6 |
| D-10 | Search implementation for tutor discovery | BR-DISC-* |
| D-11 | Anonymisation on account deletion across all data owners | BR-PRIV-1 to BR-PRIV-3 |
| D-12 | Audit log for administrative operations | BR-ADM-3 |
| D-13 | Retention periods for historical data (later) | BR-PRIV-6 |
| D-14 | Authentication and session/token handling for the target architecture. Input: the 2026-11-11 codebase architecture review | BR-ID-*, BR-PRIV-7, BR-ENV-2 |
| D-15 | Justification of every additional infrastructure component | EC-4 |

## 17. Discovery Record

**Closed questions**

| # | Topic | Resolved by |
|---|---|---|
| Q-1 | Tutor self-pause | BR-ID-9 |
| Q-2 | Holds and payments on suspension | BR-ID-10 |
| Q-3 | Booking one's own service | BR-ID-11 |
| Q-4 | Visibility during re-moderation | BR-MOD-7, BR-ID-13 |
| Q-5 | Contact details and off-platform payment in profiles | BR-ID-12 |
| Q-6 | Meeting link source, visibility and access | BR-MEET-1 to BR-MEET-5 |
| Q-7 | Notifications | BR-NOTIF-* |
| Q-8 | Tutor discovery | BR-DISC-* |
| Q-9 | Reviews | BR-REV-* |
| Q-10 | Administration scope | BR-ADM-* |
| Q-11 | Personal data | BR-PRIV-* |
| Q-12 | Environment reachability for payment events | BR-ENV-* |
| Q-13 | Engineering goals conflict | EC-1 to EC-5 |
| Q-14 | Working video vs demonstration link | BR-MEET-6, BR-MEET-7 |

No open questions remain.

**Decisions revised during discovery**

- **Tutor onboarding:** the initially agreed mandatory administrator approval of tutor profiles was **withdrawn**. It would be a bottleneck at scale, and a profile description cannot prove competence. It was replaced by automatic moderation (§12.8).
- **Moderation:** real, potentially LLM-assisted, content moderation was then **reduced to a simulation** for the MVP. Profile status stays separate from the moderation process (BR-MOD-2, BR-MOD-4).
