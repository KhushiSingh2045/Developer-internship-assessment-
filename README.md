# Harbour Stays - Hotel Booking Portal

A small full-stack hotel booking app: browse and search hotels, check availability for your dates, book a room, get a unique booking reference, then look the booking up and cancel it.

| Layer    | Technology                         |
|----------|------------------------------------|
| Backend  | Java 17, Spring Boot 3.2, Spring Data JPA, Flyway |
| Frontend | Angular 17 (standalone components) |
| Database | MySQL 8                            |

**Surprise feature: "Add to calendar".** After booking (or when managing a booking) you can download an `.ics` file that drops your stay into Google/Apple/Outlook calendar. The details page also shows an "Only N rooms left" badge when stock is low. See [DESIGN.md](DESIGN.md) for the why and how.

## What it does

- Browse hotels with name, location, description, amenities and price per night (6 sample hotels)
- Search by location and choose check-in / check-out dates
- Hotel details page with rooms available for the chosen dates
- Book one room with name + email; the **total is shown before confirming** and is calculated on the backend
- Unique booking reference (`BK-XXXXXXXX`) on confirmation
- Retrieve a booking with reference + email, and cancel it (frees the room)
- Clear validation messages and graceful error handling on both sides

## Prerequisites

- JDK 17 or newer (`java -version`)
- Maven 3.8+ (`mvn -version`), or open `backend/` in IntelliJ / VS Code and let the IDE handle Maven
- Node.js 18.13+ or 20 LTS, with npm (`node -v`)
- MySQL 8.0.16 or newer (needed so CHECK constraints are enforced)

## Run it locally

### 1. Database

```bash
mysql -u root -p < database/setup.sql
```

This creates the `hotel_db` database and a `hotel_user` account. **Edit the password in `database/setup.sql` first** and use the same one in step 2. The tables and sample data are created automatically by Flyway when the backend starts (`backend/src/main/resources/db/migration`).

### 2. Backend (port 8080)

Set the credentials as environment variables (they are not stored in the repo):

```bash
# macOS / Linux
export DB_USER=hotel_user
export DB_PASSWORD=your_password

# Windows PowerShell
$env:DB_USER="hotel_user"
$env:DB_PASSWORD="your_password"
```

Then:

```bash
cd backend
mvn spring-boot:run
```

Check it: open http://localhost:8080/api/hotels - you should see six hotels as JSON.

Optional variables: `DB_URL` (default `jdbc:mysql://localhost:3306/hotel_db...`), `CORS_ORIGINS` (default `http://localhost:4200`). See `backend/.env.example`.

### 3. Frontend (port 4200)

```bash
cd frontend
npm install
npm start
```

Open http://localhost:4200. If your backend runs on another port, change `API_URL` in `frontend/src/app/config.ts`.

### 4. Run the tests

```bash
cd backend
mvn test
```

The tests cover price calculation, date validation, the overlap rule (including back-to-back stays), "hotel full" rejection, and cancellation releasing availability. They use Mockito, so no database is needed.

## Try the main flow

1. Home page: leave the default dates and click **View rooms** on any hotel.
2. Check the nights and total, then **Book this room**, enter name and email, **Confirm booking**.
3. Note the reference, click **Add to calendar** to download the `.ics`.
4. Go to **Manage a booking**, enter reference + email, then **Cancel booking**.
5. Sold-out demo: *Palm Court Retreat* (Goa) has 1 room. Book it, then try the same dates again and you will get a clear "no rooms left" message. Cancel the first booking and it works again.

## API summary

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET  | `/api/hotels?location=` | List / search hotels |
| GET  | `/api/hotels/{id}` | One hotel |
| GET  | `/api/hotels/{id}/availability?checkIn=&checkOut=` | Rooms left, nights, total price |
| POST | `/api/bookings` | Create a booking |
| GET  | `/api/bookings/{reference}?email=` | Retrieve a booking |
| POST | `/api/bookings/{reference}/cancel?email=` | Cancel a booking |
| GET  | `/api/bookings/{reference}/calendar?email=` | Download `.ics` (surprise feature) |

Every error uses the same JSON shape: `timestamp, status, error, message, path` (plus `fieldErrors` for form validation).

## Project structure

```
backend/    Spring Boot app (controller / service / repository / entity / dto / exception / config)
frontend/   Angular app (pages, api.service.ts, models, styles)
database/   setup.sql (create DB + user); schema and sample data live in backend/.../db/migration
screenshots/  Screenshots / demo recording of the main flow
DESIGN.md   Design write-up (architecture, DB, API, booking logic, concurrency, trade-offs)
```

## Key assumptions

- One room category per hotel; each hotel has a fixed `total_rooms`.
- **Overlapping stays:** the check-out day is not a night stayed, so a guest leaving on the 10th and another arriving on the 10th do **not** conflict. Availability is the hotel's room count minus the *busiest night* of the requested stay.
- One room per booking. Maximum stay is 30 nights. Single currency (INR).
- Only `CONFIRMED` bookings use up rooms; `CANCELLED` ones are ignored.
- Booking lookup and cancellation need both reference and email; a wrong combination gives the same "not found" message.
- No authentication, payments, admin panel or cloud deployment (not required).


## Time spent and unfinished items

- Approximate time spent: 14 hours
- **Unfinished / known limitations:**  no integration test against a real MySQL, no pagination.

## AI assistance disclosure
I used an AI assistant to help scaffold the project and draft documentation. I reviewed, ran and can explain all of the code."
