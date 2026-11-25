# Personal setup and submission guide (not part of the assignment - delete before pushing if you like)

## A. First run on your machine

1. Install JDK 17, Maven, Node.js (LTS) and MySQL 8. Check with `java -version`, `mvn -version`, `node -v`, `mysql --version`.
2. Edit the password in `database/setup.sql`, then run: `mysql -u root -p < database/setup.sql`
3. Backend (new terminal):
   - macOS/Linux: `export DB_USER=hotel_user DB_PASSWORD=your_password`
   - Windows PowerShell: `$env:DB_USER="hotel_user"; $env:DB_PASSWORD="your_password"`
   - `cd backend` then `mvn spring-boot:run`
   - Open http://localhost:8080/api/hotels to confirm it works.
4. Frontend (second terminal): `cd frontend`, `npm install`, `npm start`, open http://localhost:4200
5. Tests: `cd backend` then `mvn test`

### If something goes wrong
- **Access denied for user**: the DB_USER / DB_PASSWORD variables don't match setup.sql, or you set them in a different terminal than the one running Maven.
- **Cannot reach the server (in the browser)**: backend isn't running, or it isn't on port 8080.
- **CORS error in browser console**: the Angular app must run on http://localhost:4200 (or set CORS_ORIGINS).
- **Flyway/checksum error after editing a migration**: drop and recreate the database (`DROP DATABASE hotel_db;` then re-run setup.sql). Never edit a migration that has already run; add a new `V3__...sql` instead.
- **Port 8080 or 4200 in use**: stop the other app, or use `--server.port=8081` (backend) / `ng serve --port 4300` (frontend), and update config.

## B. Git and GitHub

```bash
cd hotel-booking-portal
git init
git branch -M main
git add .
git commit -m "chore: initial project structure"
```

1. On github.com click **New repository**, name it `hotel-booking-portal`, choose **Public**, and do NOT add a README or .gitignore (you already have them).
2. Connect and push:
```bash
git remote add origin https://github.com/<your-username>/hotel-booking-portal.git
git push -u origin main
```
3. Make small commits as you improve things, e.g. `git commit -am "fix: ..."` then `git push`.
4. Before you submit: open the repo in a private/incognito window to confirm it is public, and search it for passwords (`DB_PASSWORD`, real credentials). `change_me` placeholders are fine.

## C. Before you send it

- [ ] Run the app end to end and take the screenshots listed in `screenshots/README.md`
- [ ] Fill in time spent, unfinished items and AI disclosure in README.md
- [ ] Read DESIGN.md and make sure you can explain every section in your own words
- [ ] `mvn test` passes
- [ ] Repo is public, resume saved as PDF
- [ ] Email subject exactly: `091126 Developer Internship Assessment : <Your Full Name>` (see SUBMISSION_EMAIL.md)
