-- Sample data: six hotels (Palm Court has only 1 room, handy for demoing "sold out")
INSERT INTO hotels (name, location, description, amenities, price_per_night, total_rooms) VALUES
('Marine Drive Residency', 'Mumbai',    'Sea-facing rooms on the Queen''s Necklace with a quiet rooftop lounge.', 'WiFi, Breakfast, Air conditioning, Rooftop lounge', 6500.00, 8),
('Palm Court Retreat',     'Goa',       'Boutique villa hotel ten minutes from the beach, only a handful of rooms.', 'WiFi, Pool, Breakfast, Bike rental', 4800.00, 1),
('Amber Fort Haveli',      'Jaipur',    'Restored haveli with courtyard dining and views of the old city walls.', 'WiFi, Courtyard dining, Spa, Airport pickup', 5200.00, 5),
('Connaught Business Inn', 'Delhi',     'Practical rooms near Connaught Place, built for short work trips.', 'WiFi, Gym, Breakfast, Meeting room', 4100.00, 10),
('Garden City Suites',     'Bengaluru', 'Quiet suites beside Cubbon Park with fast WiFi and a good coffee bar.', 'WiFi, Coffee bar, Gym, Parking', 3900.00, 6),
('Lakeview Palace Stay',   'Udaipur',   'Lake-facing rooms in a heritage building with sunset terrace dinners.', 'WiFi, Lake view, Breakfast, Terrace dining', 7400.00, 3);
