# Design Write-up

## 1. Architecture

```
  Angular (port 4200)  --HTTP/JSON-->  Spring Boot REST API (port 8080)  --JDBC/JPA-->  MySQL (hotel_db)
  pages + ApiService                   controller -> service -> repository              hotels, bookings
```

- **Frontend:** Angular standalone components. Pages: search, hotel details, booking form, confirmation, manage booking. A single `ApiService` owns every HTTP call; components only hold view state.
- **Backend:** layered. Controllers handle HTTP only, services hold the business rules, repositories talk to MySQL, DTOs (Java records) keep entities out of the API, and a `@RestControllerAdvice` turns exceptions into one consistent error shape.
- **Database:** MySQL, with the schema owned by Flyway migrations so a fresh machine gets identical tables and sample data. Hibernate is set to `ddl-auto=none`.
- **Config and secrets:** DB credentials and allowed CORS origins come from environment variables. Nothing secret is in the repo.

## 2. Database design

**hotels**: `id` PK, `name`, `location` (indexed), `description`, `amenities` (comma-separated text), `price_per_night DECIMAL(10,2)`, `total_rooms`.
**bookings**: `id` PK, `reference` UNIQUE, `hotel_id` FK, `guest_name`, `guest_email`, `check_in`, `check_out`, `nights`, `total_amount DECIMAL(10,2)`, `status` (`CONFIRMED`/`CANCELLED`), `created_at`.

Relationship: one hotel has many bookings.

Important constraints: unique booking reference; foreign key bookings to hotels; `CHECK (check_out > check_in)`; `CHECK (price_per_night > 0)`; `CHECK (total_rooms > 0)`; status limited to two values; composite index `(hotel_id, status, check_in, check_out)` for the availability query. Money uses `DECIMAL`, never floating point.

**Availability is derived, not stored.** The brief mentions "room availability" data. I derive it from `total_rooms` and the confirmed bookings, rather than keeping a separate counter or per-night table. That means there is no second number that can drift out of sync, and cancelling needs no extra bookkeeping.

## 3. API design

| Endpoint | Purpose | Notable errors |
|----------|---------|----------------|
| `GET /api/hotels?location=` | List or search hotels (case-insensitive contains) | |
| `GET /api/hotels/{id}` | Hotel details | 404 |
| `GET /api/hotels/{id}/availability?checkIn&checkOut` | Rooms left, nights, backend-calculated total | 400 dates, 404 |
| `POST /api/bookings` | Create booking, returns 201 with reference | 400 validation, 404, 409 sold out |
| `GET /api/bookings/{ref}?email=` | Retrieve a booking | 404 |
| `POST /api/bookings/{ref}/cancel?email=` | Cancel | 404, 409 already cancelled |
| `GET /api/bookings/{ref}/calendar?email=` | `.ics` download (surprise feature) | 404, 409 if cancelled |

Error shape for all failures: `{ timestamp, status, error, message, path, fieldErrors? }`. Status codes: 400 invalid input or rule broken, 404 not found, 409 conflict (no rooms, already cancelled), 500 unexpected (details logged, a generic message returned).

## 4. Booking logic

- **Date validation:** check-in cannot be in the past (today is allowed); check-out must be after check-in; maximum 30 nights. Enforced in the backend (`BookingService.validateDates`); the frontend repeats the checks only for faster feedback. "Today" comes from an injected `Clock`, so tests can freeze time.
- **Pricing:** `nights = days between check-in and check-out`; `total = price_per_night x nights`, rounded to 2 decimals, in `PricingService`. The client never sends a price; the booking stores the total that was calculated.
- **Overlap rule:** a night is occupied if `existing.checkIn <= night < existing.checkOut`. The check-out day is not a night, so back-to-back stays are fine. The repository fetches confirmed bookings with `existing.checkIn < new.checkOut AND existing.checkOut > new.checkIn`, then `AvailabilityCalculator` takes the **busiest night** of the stay. Taking the maximum matters: two bookings (day 1-3 and day 5-7) both fall inside a day 1-7 request but never share a night, so they use one room, not two.
- **Availability:** `available = total_rooms - busiest night's occupancy`. Booking is refused (409) at 0.
- **Cancellation:** requires reference and email. It sets `status = CANCELLED`; cancelled bookings are excluded from every availability count, so the room is released immediately. Cancelling twice returns 409.
- **Reference:** `BK-` plus 8 characters from a random UUID, checked for uniqueness (with retries), and backed by a UNIQUE index.

## 5. Simultaneous bookings (two users, one last room)

**The problem:** two requests both read "1 room left", both pass the check, both insert, and the hotel is overbooked.

**What I implemented: pessimistic locking on the hotel row.** `create()` runs in a transaction and first loads the hotel with `SELECT ... FOR UPDATE` (`@Lock(PESSIMISTIC_WRITE)`). The second request for the same hotel blocks until the first commits. It then re-reads the bookings, sees the room is gone, and returns a clean 409 "no rooms left". Different hotels do not block each other.

**Alternatives considered:**
- *Optimistic locking* (a `@Version` column plus retry): no blocking, but the version has to live on something both transactions touch, and needs retry code.
- *Per-night inventory table* with an atomic `UPDATE ... SET remaining = remaining - 1 WHERE remaining > 0`: very scalable and precise, but more schema, and multi-night stays need multi-row updates in a fixed order to avoid deadlocks.
- *Serializable isolation*: simple to reason about, but causes deadlocks and retries under load.
- *Queue or distributed lock* (Redis etc.): useful at multi-instance scale, overkill here.

**Trade-off:** locking serialises bookings per hotel. That is fine at this scale and gives the strongest guarantee for the least code. The honest gap: I unit-tested the logic with mocks, but did not write an automated multi-threaded test against real MySQL (listed below).

## 6. Design decisions and trade-offs

- **Derive availability instead of storing it** (above): simpler and cannot drift, but the availability query grows with bookings. Acceptable with the composite index; a per-night table is the upgrade path.
- **Flyway migrations instead of Hibernate auto-DDL:** repeatable schema and real SQL constraints, at the cost of one extra dependency.
- **DTO records instead of exposing entities:** a slightly bigger code base, but the API contract stays stable and nothing lazy or internal leaks.
- **Amenities as a comma-separated column:** fine for display-only data. A separate table would be needed to filter by amenity.
- **Same "not found" message for wrong reference or wrong email:** stops people guessing references. There is no authentication, so the reference plus email act as the shared secret.
- **Cancel is a status change, not a delete:** keeps history and keeps references unique forever.
- **Mockito unit tests instead of full integration tests:** fast and no database needed, covering the rules the brief asks for; they cannot prove the SQL or the locking.

## 7. Surprise feature: "Add to calendar" (plus a low-stock badge)

**What it does:** after booking, or from Manage booking, one click downloads an `.ics` file that adds the stay to any calendar app as an all-day event with the hotel, location and reference. The hotel page also shows "Only N rooms left" when 1-2 rooms remain.

**Why:** guests book weeks ahead and then forget the details. A calendar entry solves a real problem with no extra accounts or email integration, and the badge helps guests decide while stock is low.

**How:** `CalendarService` builds a standards-compliant iCalendar text (CRLF line endings, escaped text, `DTEND` set to the check-out date because it is exclusive in iCalendar). `GET /api/bookings/{ref}/calendar` requires reference plus email and refuses cancelled bookings. The Angular app requests it as a `Blob` and saves it with a temporary link. The badge reads the `availableRooms` value the availability endpoint already returns.

## 8. Limitations and what I would improve

- No automated test against real MySQL (Testcontainers) to prove the locking and the overlap query end to end.
- One room category per hotel; real hotels need room types, each with its own inventory.
- No authentication, payments, or confirmation emails.
- Search is by location only (no filters by price or amenity), no pagination.
- Cancellation has no policy (cut-off times, refunds) and works even for past stays.
- Single currency; prices are not timezone or tax aware.
- Frontend has no automated tests; styles are one global stylesheet.
- With more time: Docker Compose for one-command setup, OpenAPI docs, a per-night inventory table, rate limiting on the lookup endpoints, and CI that runs the tests.
