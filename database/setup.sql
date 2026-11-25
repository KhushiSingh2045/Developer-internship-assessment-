-- Run ONCE as the MySQL root user, e.g.:  mysql -u root -p < database/setup.sql
-- Replace the password below with your own. Do not commit your real password.
CREATE DATABASE IF NOT EXISTS hotel_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'hotel_user'@'localhost' IDENTIFIED BY 'change_me';
GRANT ALL PRIVILEGES ON hotel_db.* TO 'hotel_user'@'localhost';
FLUSH PRIVILEGES;

-- The tables and sample hotels are created automatically by Flyway the first time the backend starts:
--   backend/src/main/resources/db/migration/V1__schema.sql       (tables + constraints)
--   backend/src/main/resources/db/migration/V2__sample_data.sql  (6 sample hotels)
