-- Hotel Booking Portal: schema (MySQL 8.0.16+ so CHECK constraints are enforced)

CREATE TABLE hotels (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    name            VARCHAR(150)  NOT NULL,
    location        VARCHAR(100)  NOT NULL,
    description     TEXT,
    amenities       VARCHAR(500),
    price_per_night DECIMAL(10,2) NOT NULL,
    total_rooms     INT           NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_hotels_price CHECK (price_per_night > 0),
    CONSTRAINT chk_hotels_rooms CHECK (total_rooms > 0),
    INDEX idx_hotels_location (location)
) ENGINE=InnoDB;

CREATE TABLE bookings (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    reference    VARCHAR(20)   NOT NULL,
    hotel_id     BIGINT        NOT NULL,
    guest_name   VARCHAR(100)  NOT NULL,
    guest_email  VARCHAR(150)  NOT NULL,
    check_in     DATE          NOT NULL,
    check_out    DATE          NOT NULL,
    nights       INT           NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status       VARCHAR(15)   NOT NULL DEFAULT 'CONFIRMED',
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_bookings_reference (reference),
    CONSTRAINT fk_bookings_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id),
    CONSTRAINT chk_bookings_dates CHECK (check_out > check_in),
    CONSTRAINT chk_bookings_status CHECK (status IN ('CONFIRMED', 'CANCELLED')),
    INDEX idx_bookings_hotel_dates (hotel_id, status, check_in, check_out)
) ENGINE=InnoDB;
