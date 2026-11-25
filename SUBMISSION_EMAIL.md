**Subject:** 091126 Developer Internship Assessment : <Your Full Name>

**Attachments:** Resume (PDF)

---

Hi [Hiring Team],

Please find my submission for the Hotel Booking Portal assessment.

**GitHub repository (public):** https://github.com/<your-username>/hotel-booking-portal
**Resume:** attached (PDF)

**Approach taken**
I designed the database and the booking rules first, then built the Spring Boot backend in layers (controller, service, repository) with unit tests for pricing, date validation and availability. After testing the APIs, I built the Angular frontend around the main flow: search, availability and price, booking, confirmation, then retrieve and cancel. I finished with the README, DESIGN.md and screenshots.

**Key assumptions**
- One room category per hotel with a fixed number of rooms.
- The check-out day is not a night stayed, so back-to-back stays do not overlap; availability is the room count minus the busiest night of the stay.
- Only confirmed bookings use up rooms; cancelling releases them.
- One room per booking, maximum 30 nights, single currency.
- Retrieving or cancelling a booking needs both the reference and the email.

**Challenges and how I addressed them**
- *Two users booking the last room:* I lock the hotel row inside the booking transaction so requests for the same hotel are handled one at a time and the second gets a clear "no rooms left" message.
- *Correct overlap logic:* I count confirmed bookings per night and take the maximum, and wrote tests for the edge cases (back-to-back stays, cancelled bookings, non-overlapping bookings inside one request).
- *Consistent errors:* a global exception handler returns one JSON error shape, and the frontend shows those messages to the user.
- _[Add any real problem you hit while running it, and how you fixed it.]_

**Surprise feature**
"Add to calendar": after booking, the guest downloads an .ics file that adds the stay to any calendar app. I chose it because guests forget booking details, and it needs no email service or accounts. The backend generates the iCalendar file and the frontend downloads it. The hotel page also shows an "Only N rooms left" badge.

**With more time I would add**
Integration tests against real MySQL (Testcontainers) including a concurrent-booking test, room types with separate inventory, authentication, payments and confirmation emails, search filters and pagination, Docker Compose for one-command setup, and CI.

Time spent: approximately [X] hours. [Mention any unfinished items.] [Disclose AI assistance.]

Thank you for the opportunity.

Best regards,
[Your Name]
[Phone] | [LinkedIn]
