-- SkyLink development seed data. Passwords are encoded by DataSeeder on startup.
CREATE TABLE IF NOT EXISTS persistent_logins (
    username VARCHAR(64) NOT NULL,
    series VARCHAR(64) PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMP NOT NULL
);

INSERT IGNORE INTO roles (id, name) VALUES
    (1, 'ROLE_SUPER_ADMIN'),
    (2, 'ROLE_ADMIN'),
    (3, 'ROLE_STAFF');

INSERT IGNORE INTO app_users
    (id, full_name, email, password, phone, status, created_at)
VALUES
    (1, 'Super Admin', 'superadmin@skylink.com', 'Admin@1234', '01700000001', 'ACTIVE', NOW()),
    (2, 'System Admin', 'admin@skylink.com', 'Admin@1234', '01700000002', 'ACTIVE', NOW()),
    (3, 'Reception Staff', 'staff@skylink.com', 'Admin@1234', '01700000003', 'ACTIVE', NOW());

INSERT IGNORE INTO user_roles (user_id, role_id) VALUES
    (1, 1),
    (2, 2),
    (3, 3);

INSERT IGNORE INTO airports (id, iata_code, name, city, country) VALUES
    (1, 'DAC', 'Hazrat Shahjalal International Airport', 'Dhaka', 'Bangladesh'),
    (2, 'CGP', 'Shah Amanat International Airport', 'Chittagong', 'Bangladesh'),
    (3, 'ZYL', 'Osmani International Airport', 'Sylhet', 'Bangladesh'),
    (4, 'JSR', 'Jessore Airport', 'Jessore', 'Bangladesh'),
    (5, 'CXB', 'Cox''s Bazar Airport', 'Cox''s Bazar', 'Bangladesh'),
    (6, 'DXB', 'Dubai International Airport', 'Dubai', 'UAE'),
    (7, 'DOH', 'Hamad International Airport', 'Doha', 'Qatar'),
 (8, 'SIN', 'Changi Airport', 'Singapore', 'Singapore');

INSERT IGNORE INTO aircraft
    (id, registration_number, model_name, manufacturer,
     aircraft_type_code, total_seats, economy_seats,
     business_seats, first_class_seats, status, created_at)
VALUES
    (1, 'S2-AKA', 'Boeing 737-800', 'Boeing', 'B738', 162, 138, 24, 0, 'ACTIVE', NOW()),
    (2, 'S2-AGB', 'Airbus A320-200', 'Airbus', 'A320', 150, 126, 24, 0, 'ACTIVE', NOW()),
    (3, 'S2-AJB', 'Boeing 737 MAX 8', 'Boeing', 'B38M', 162, 138, 24, 0, 'MAINTENANCE', NOW()),
    (4, 'S2-AGP', 'Airbus A321-200', 'Airbus', 'A321', 188, 156, 32, 0, 'ACTIVE', NOW()),
    (5, 'S2-AHM', 'De Havilland Q400', 'De Havilland', 'DH8D', 74, 74, 0, 0, 'ACTIVE', NOW()),
    (6, 'S2-AFF', 'Boeing 777-300ER', 'Boeing', 'B77W', 396, 304, 70, 22, 'ACTIVE', NOW()),
    (7, 'S2-AJV', 'Airbus A330-200', 'Airbus', 'A332', 251, 197, 42, 12, 'RETIRED', NOW()),
    (8, 'S2-AGQ', 'ATR 72-600', 'ATR', 'AT76', 68, 68, 0, 0, 'MAINTENANCE', NOW());

INSERT INTO flights
    (id, flight_number, origin_airport_id, destination_airport_id,
     departure_time, arrival_time, aircraft_id, status,
     economy_price, business_price, first_class_price,
     available_economy_seats, available_business_seats,
     available_first_class_seats, created_at)
VALUES
    (1, 'BS-141', 1, 2, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), 1, 'SCHEDULED', 3500.00, 7000.00, 9000.00, 120, 20, 0, NOW()),
    (2, 'BS-142', 2, 1, DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 2, 'SCHEDULED', 3500.00, 7000.00, 9000.00, 100, 18, 0, NOW()),
    (3, 'BG-022', 1, 3, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY), 1, 'SCHEDULED', 4200.00, 8500.00, 10500.00, 130, 22, 0, NOW()),
    (4, 'BS-305', 1, 4, DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY), 5, 'SCHEDULED', 2800.00, 5200.00, 6500.00, 70, 0, 0, NOW()),
    (5, 'BG-101', 1, 5, DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY), 5, 'SCHEDULED', 2500.00, 4800.00, 6000.00, 60, 0, 0, NOW()),
    (6, 'BS-212', 3, 1, DATE_ADD(NOW(), INTERVAL 6 DAY), DATE_ADD(NOW(), INTERVAL 6 DAY), 2, 'SCHEDULED', 4200.00, 8500.00, 10500.00, 110, 20, 0, NOW()),
    (7, 'BS-401', 1, 6, DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 6, 'SCHEDULED', 18000.00, 35000.00, 65000.00, 280, 60, 18, NOW()),
    (8, 'BG-502', 1, 7, DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY), 6, 'SCHEDULED', 16000.00, 32000.00, 58000.00, 300, 68, 20, NOW()),
    (9, 'BS-100', 1, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 23 HOUR), 1, 'ARRIVED', 3500.00, 7000.00, 9000.00, 0, 0, 0, NOW()),
    (10, 'BS-101', 2, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 47 HOUR), 2, 'ARRIVED', 3500.00, 7000.00, 9000.00, 0, 0, 0, NOW()),
    (11, 'BG-020', 1, 3, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 71 HOUR), 1, 'ARRIVED', 4200.00, 8500.00, 10500.00, 0, 0, 0, NOW()),
    (12, 'BS-300', 1, 4, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 95 HOUR), 5, 'ARRIVED', 2800.00, 5200.00, 6500.00, 0, 0, 0, NOW()),
    (13, 'BG-099', 1, 5, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 119 HOUR), 5, 'CANCELLED', 2500.00, 4800.00, 6000.00, 60, 0, 0, NOW()),
    (14, 'BS-210', 3, 1, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 143 HOUR), 2, 'ARRIVED', 4200.00, 8500.00, 10500.00, 0, 0, 0, NOW()),
    (15, 'BS-199', 1, 2, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 167 HOUR), 4, 'ARRIVED', 3500.00, 7000.00, 9000.00, 0, 0, 0, NOW())
ON DUPLICATE KEY UPDATE
    departure_time = VALUES(departure_time),
    arrival_time = VALUES(arrival_time),
    economy_price = VALUES(economy_price),
    business_price = VALUES(business_price),
    first_class_price = VALUES(first_class_price),
    available_economy_seats = VALUES(available_economy_seats),
    available_business_seats = VALUES(available_business_seats),
    available_first_class_seats = VALUES(available_first_class_seats);

INSERT INTO flights
    (id, flight_number, origin_airport_id, destination_airport_id,
     departure_time, arrival_time, aircraft_id, status,
     economy_price, business_price, first_class_price,
     available_economy_seats, available_business_seats,
     available_first_class_seats, created_at)
VALUES
    (16, 'BS-201', 1, 2, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), 1, 'SCHEDULED', 3500.00, 7000.00, 9000.00, 138, 24, 0, NOW()),
    (17, 'BS-202', 2, 1, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), 2, 'SCHEDULED', 3500.00, 7000.00, 9000.00, 126, 24, 0, NOW()),
    (18, 'BS-203', 3, 1, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), 5, 'SCHEDULED', 2800.00, 5200.00, 6500.00, 74, 0, 0, NOW()),
    (19, 'BS-204', 6, 1, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), 6, 'SCHEDULED', 18000.00, 35000.00, 65000.00, 304, 70, 22, NOW()),
    (20, 'BS-211', 1, 8, DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 6, 'SCHEDULED', 21000.00, 40000.00, 75000.00, 304, 70, 22, NOW()),
    (21, 'BS-213', 4, 1, DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 5, 'SCHEDULED', 2600.00, 5000.00, 6000.00, 74, 0, 0, NOW()),
    (22, 'BS-214', 5, 1, DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 5, 'SCHEDULED', 2800.00, 5200.00, 6500.00, 74, 0, 0, NOW()),
    (23, 'BS-215', 7, 1, DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 6, 'SCHEDULED', 17000.00, 33000.00, 60000.00, 304, 70, 22, NOW()),
    (24, 'BS-221', 1, 6, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY), 6, 'SCHEDULED', 18000.00, 35000.00, 65000.00, 304, 70, 22, NOW()),
    (25, 'BS-222', 1, 3, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY), 5, 'SCHEDULED', 2800.00, 5200.00, 6500.00, 74, 0, 0, NOW()),
    (26, 'BS-223', 8, 1, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY), 6, 'SCHEDULED', 21000.00, 40000.00, 75000.00, 304, 70, 22, NOW()),
    (27, 'BS-224', 2, 8, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY), 4, 'SCHEDULED', 20000.00, 38000.00, 70000.00, 156, 32, 0, NOW()),
    (28, 'BS-231', 1, 7, DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY), 6, 'SCHEDULED', 17000.00, 33000.00, 60000.00, 304, 70, 22, NOW()),
    (29, 'BS-232', 3, 1, DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY), 5, 'SCHEDULED', 2800.00, 5200.00, 6500.00, 74, 0, 0, NOW()),
    (30, 'BS-233', 5, 1, DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY), 5, 'SCHEDULED', 2800.00, 5200.00, 6500.00, 74, 0, 0, NOW()),
    (31, 'BS-234', 4, 2, DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY), 5, 'SCHEDULED', 3000.00, 5600.00, 7000.00, 74, 0, 0, NOW()),
    (32, 'BS-235', 1, 5, DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY), 5, 'SCHEDULED', 2800.00, 5200.00, 6500.00, 74, 0, 0, NOW())
ON DUPLICATE KEY UPDATE
    departure_time = VALUES(departure_time),
    arrival_time = VALUES(arrival_time),
    economy_price = VALUES(economy_price),
    business_price = VALUES(business_price),
    first_class_price = VALUES(first_class_price),
    available_economy_seats = VALUES(available_economy_seats),
    available_business_seats = VALUES(available_business_seats),
    available_first_class_seats = VALUES(available_first_class_seats);

INSERT IGNORE INTO customers
    (id, full_name, email, phone, nid_or_passport, date_of_birth, address, registered_at)
VALUES
    (1, 'Rafiq Hossain', 'rafiq.hossain@email.com', '01712345601', 'NID198801', '1988-05-15', 'Mirpur, Dhaka', NOW()),
    (2, 'Nusrat Jahan', 'nusrat.jahan@email.com', '01812345602', 'NID199002', '1990-08-22', 'Gulshan, Dhaka', NOW()),
    (3, 'Tariq Islam', 'tariq.islam@email.com', '01912345603', 'NID198503', '1985-03-10', 'Chittagong City', NOW()),
    (4, 'Sultana Begum', 'sultana.begum@email.com', '01712345604', 'NID199204', '1992-11-30', 'Sylhet Sadar', NOW()),
    (5, 'Karim Uddin', 'karim.uddin@email.com', '01612345605', 'NID198705', '1987-07-18', 'Jessore Town', NOW()),
    (6, 'Fatema Akter', 'fatema.akter@email.com', '01812345606', 'NID199506', '1995-01-25', 'Banani, Dhaka', NOW()),
    (7, 'Mohammed Ali', 'mohammed.ali@email.com', '01712345607', 'NID198307', '1983-09-12', 'Old Dhaka', NOW()),
    (8, 'Rashida Khatun', 'rashida.khatun@email.com', '01912345608', 'NID199108', '1991-04-08', 'Cox''s Bazar Town', NOW()),
    (9, 'Shahadat Hossain', 'shahadat@email.com', '01612345609', 'NID198609', '1986-12-20', 'Uttara, Dhaka', NOW()),
    (10, 'Nasrin Akhter', 'nasrin.akhter@email.com', '01712345610', 'NID199310', '1993-06-14', 'Dhanmondi, Dhaka', NOW()),
    (11, 'Jabbar Mia', 'jabbar.mia@email.com', '01812345611', 'NID198011', '1980-02-28', 'Comilla Sadar', NOW()),
    (12, 'Roksana Parvin', 'roksana.parvin@email.com', '01912345612', 'NID199712', '1997-10-05', 'Narayanganj', NOW()),
    (13, 'Delwar Hossain', 'delwar.hossain@email.com', '01612345613', 'NID198213', '1982-08-16', 'Bogra Town', NOW()),
    (14, 'Morjina Begum', 'morjina.begum@email.com', '01712345614', 'NID199614', '1996-03-22', 'Rajshahi City', NOW()),
    (15, 'Shafiqul Islam', 'shafiqul@email.com', '01812345615', 'NID197815', '1978-11-09', 'Khulna City', NOW()),
    (16, 'Ayesha Siddika', 'ayesha.siddika@email.com', '01912345616', 'NID199916', '1999-07-31', 'Barisal City', NOW()),
    (17, 'Nurul Huda', 'nurul.huda@email.com', '01612345617', 'NID198117', '1981-05-03', 'Rangpur City', NOW()),
    (18, 'Salma Begum', 'salma.begum@email.com', '01712345618', 'NID199418', '1994-09-17', 'Faridpur Sadar', NOW()),
    (19, 'Aminul Islam', 'aminul.islam@email.com', '01812345619', 'NID198919', '1989-01-11', 'Tangail Sadar', NOW()),
    (20, 'Kohinur Akter', 'kohinur.akter@email.com', '01912345620', 'NID200020', '2000-12-25', 'Mymensingh City', NOW());

INSERT IGNORE INTO bookings
    (id, booking_reference, flight_id, customer_id, created_by_user_id,
     seat_class, passenger_count, total_amount, status, created_at)
VALUES
    (1, 'SKY-20260001', 1, 1, 2, 'ECONOMY', 1, 3500.00, 'CONFIRMED', NOW()),
    (2, 'SKY-20260002', 2, 2, 3, 'BUSINESS', 1, 7000.00, 'PENDING', NOW()),
    (3, 'SKY-20260003', 3, 3, 2, 'ECONOMY', 2, 8400.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
    (4, 'SKY-20260004', 4, 4, 3, 'ECONOMY', 1, 2800.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
    (5, 'SKY-20260005', 5, 5, 2, 'ECONOMY', 3, 7500.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
    (6, 'SKY-20260006', 9, 6, 2, 'ECONOMY', 1, 3500.00, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (7, 'SKY-20260007', 9, 7, 3, 'BUSINESS', 1, 7000.00, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (8, 'SKY-20260008', 10, 8, 2, 'ECONOMY', 2, 7000.00, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (9, 'SKY-20260009', 10, 9, 3, 'ECONOMY', 1, 3500.00, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (10, 'SKY-20260010', 11, 10, 2, 'BUSINESS', 2, 17000.00, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (11, 'SKY-20260011', 11, 11, 3, 'ECONOMY', 1, 4200.00, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (12, 'SKY-20260012', 12, 12, 2, 'ECONOMY', 2, 5600.00, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (13, 'SKY-20260013', 12, 13, 3, 'ECONOMY', 1, 2800.00, 'CANCELLED', DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (14, 'SKY-20260014', 13, 14, 2, 'ECONOMY', 1, 2500.00, 'CANCELLED', DATE_SUB(NOW(), INTERVAL 4 DAY)),
    (15, 'SKY-20260015', 14, 15, 3, 'BUSINESS', 1, 8500.00, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 4 DAY)),
    (16, 'SKY-20260016', 14, 16, 2, 'ECONOMY', 3, 12600.00, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 5 DAY)),
    (17, 'SKY-20260017', 14, 17, 3, 'ECONOMY', 1, 3500.00, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 5 DAY)),
    (18, 'SKY-20260018', 15, 18, 2, 'BUSINESS', 2, 14000.00, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 6 DAY)),
    (19, 'SKY-20260019', 9, 19, 3, 'ECONOMY', 1, 3500.00, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 6 DAY)),
    (20, 'SKY-20260020', 10, 20, 2, 'ECONOMY', 2, 7000.00, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 6 DAY)),
    (21, 'SKY-20260021', 6, 1, 2, 'BUSINESS', 1, 8500.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
    (22, 'SKY-20260022', 7, 2, 3, 'ECONOMY', 2, 36000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
    (23, 'SKY-20260023', 8, 3, 2, 'BUSINESS', 1, 32000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
    (24, 'SKY-20260024', 6, 4, 3, 'ECONOMY', 1, 18000.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
    (25, 'SKY-20260025', 7, 5, 2, 'ECONOMY', 3, 48000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
    (26, 'SKY-20260026', 1, 6, 3, 'ECONOMY', 1, 3500.00, 'BOARDED', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
    (27, 'SKY-20260027', 2, 7, 2, 'BUSINESS', 1, 7000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
    (28, 'SKY-20260028', 3, 8, 3, 'ECONOMY', 2, 8400.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
    (29, 'SKY-20260029', 4, 9, 2, 'ECONOMY', 1, 2800.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 7 HOUR)),
    (30, 'SKY-20260030', 5, 10, 3, 'ECONOMY', 2, 5000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 8 HOUR));

INSERT IGNORE INTO activity_logs
    (id, actor_email, actor_name, action, entity_type, entity_id, detail, created_at)
VALUES
    (1, 'admin@skylink.com', 'System Admin', 'LOGIN', 'USER', '2', 'Successful sign in', DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
    (2, 'admin@skylink.com', 'System Admin', 'CREATE', 'BOOKING', '1', 'Created booking SKY-20260001', DATE_SUB(NOW(), INTERVAL 4 MINUTE)),
    (3, 'staff@skylink.com', 'Reception Staff', 'UPDATE', 'BOOKING', '2', 'Marked booking SKY-20260002 pending', DATE_SUB(NOW(), INTERVAL 3 MINUTE)),
    (4, 'admin@skylink.com', 'System Admin', 'CREATE', 'CUSTOMER', '1', 'Registered customer Rafiq Hossain', DATE_SUB(NOW(), INTERVAL 2 MINUTE)),
    (5, 'superadmin@skylink.com', 'Super Admin', 'UPDATE', 'AIRCRAFT', '6', 'Updated seat config for S2-AFF', DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
    (6, 'admin@skylink.com', 'System Admin', 'CREATE', 'FLIGHT', '7', 'Created flight BS-401', DATE_SUB(NOW(), INTERVAL 30 SECOND)),
    (7, 'staff@skylink.com', 'Reception Staff', 'DELETE', 'BOOKING', '13', 'Cancelled booking SKY-20260013', DATE_SUB(NOW(), INTERVAL 20 SECOND)),
    (8, 'admin@skylink.com', 'System Admin', 'CREATE', 'USER', '3', 'Added staff account', DATE_SUB(NOW(), INTERVAL 10 SECOND)),
    (9, 'superadmin@skylink.com', 'Super Admin', 'UPDATE', 'USER', '2', 'Reset admin password', DATE_SUB(NOW(), INTERVAL 5 SECOND)),
    (10, 'admin@skylink.com', 'System Admin', 'LOGIN', 'USER', '2', 'Successful sign in', NOW());

INSERT IGNORE INTO booking_status_history
    (id, booking_id, status, changed_by, changed_by_name, note, changed_at)
VALUES
    (1, 1, 'PENDING', 'admin@skylink.com', 'System Admin', 'Booking created', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
    (2, 1, 'CONFIRMED', 'admin@skylink.com', 'System Admin', 'Payment confirmed', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
    (3, 2, 'PENDING', 'staff@skylink.com', 'Reception Staff', 'Booking created', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
    (4, 2, 'CONFIRMED', 'staff@skylink.com', 'Reception Staff', 'Payment received', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
    (5, 3, 'PENDING', 'admin@skylink.com', 'System Admin', 'Booking created', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
    (6, 3, 'CONFIRMED', 'admin@skylink.com', 'System Admin', 'Confirmed for 2 passengers', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
    (7, 6, 'PENDING', 'admin@skylink.com', 'System Admin', 'Booking created', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (8, 6, 'CONFIRMED', 'admin@skylink.com', 'System Admin', 'Payment confirmed', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (9, 6, 'COMPLETED', 'admin@skylink.com', 'System Admin', 'Flight completed', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (10, 13, 'PENDING', 'staff@skylink.com', 'Reception Staff', 'Booking created', DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (11, 13, 'CONFIRMED', 'staff@skylink.com', 'Reception Staff', 'Payment received', DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (12, 13, 'CANCELLED', 'admin@skylink.com', 'System Admin', 'Cancelled by customer request', DATE_SUB(NOW(), INTERVAL 2 DAY));
