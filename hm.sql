-- Create the database (student1)
CREATE DATABASE IF NOT EXISTS sys;
USE sys;

-- Create the users table (with admin and regular user roles)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('user', 'admin') DEFAULT 'user'
);

-- Create the rooms table (with an ID for room types)
CREATE TABLE IF NOT EXISTS rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,  -- ID for each room
    room_type ENUM('Single', 'Double', 'Suite') NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    status ENUM('Available', 'Booked', 'Under Maintenance') DEFAULT 'Available'
);

-- Create the bookings table (with a foreign key for room ID)
CREATE TABLE IF NOT EXISTS bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    room_id INT,  -- Foreign key that will reference rooms.id
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id)  -- Room ID references rooms.id
);

-- Sample data for users (admin and regular users)
INSERT IGNORE INTO users (username, password, role) VALUES
('aravind', 'ara', 'admin'),
('user1', 'password1', 'user'),
('user2', 'password2', 'user');

-- Sample data for rooms (Single, Double, and Suite rooms)
INSERT INTO rooms (room_type, price, status) VALUES
('Single', 100.00, 'Available'),
('Double', 150.00, 'Available'),
('Suite', 250.00, 'Available'),
('Single', 100.00, 'Available'),
('Double', 150.00, 'Under Maintenance'),
('Suite', 250.00, 'Available');

-- Sample data for bookings (in pending status)
INSERT INTO bookings (user_id, room_id, check_in_date, check_out_date, status) VALUES
(2, 1, '2025-05-01', '2025-05-05', 'PENDING'),  -- Room 1 (Single)
(3, 2, '2025-06-10', '2025-06-15', 'PENDING'); -- Room 2 (Double)
