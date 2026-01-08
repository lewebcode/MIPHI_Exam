-- Create Hotels table
CREATE TABLE IF NOT EXISTS hotels (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL
);

-- Create Rooms table
CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    number VARCHAR(20) NOT NULL,
    available BOOLEAN DEFAULT TRUE,
    times_booked INT DEFAULT 0,
    FOREIGN KEY (hotel_id) REFERENCES hotels(id)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_rooms_hotel_id ON rooms(hotel_id);
CREATE INDEX IF NOT EXISTS idx_rooms_available ON rooms(available);
