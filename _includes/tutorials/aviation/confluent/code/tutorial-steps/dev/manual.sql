-- For the purposes of this recipe when testing by inserting records manually,
--  a short pause between these insert groups is required. This allows
--  the flight data to be processed by the customer_flights_rekeyed
--  table before the JOIN with the flight updates data

INSERT INTO customers (id, name, address, email, phone, loyalty_status, loyalty_id) VALUES (1, 'Gleda Lealle', '93 Express Point', 'glealle0@senate.gov', '+351 831 301 6746', 'Silver', '2PLNX338063');
INSERT INTO customers (id, name, address, email, phone, loyalty_status, loyalty_id) VALUES (2, 'Gilly Crocombe', '332 Blaine Avenue', 'gcrocombe1@homestead.com', '+33 203 565 3736', 'Silver', '7AWLM918339');
INSERT INTO customers (id, name, address, email, phone, loyalty_status, loyalty_id) VALUES (3, 'Astrix Aspall', '56 Randy Place', 'aaspall2@ebay.co.uk', '+33 679 296 6645', 'Gold', '3RNZH870911');
INSERT INTO customers (id, name, address, email, phone, loyalty_status, loyalty_id) VALUES (4, 'Ker Omond', '23255 Tennessee Court', 'komond3@usnews.com', '+33 515 323 0170', 'Silver', '5BWEP418137');
INSERT INTO customers (id, name, address, email, phone, loyalty_status, loyalty_id) VALUES (5, 'Arline Synnott', '144 Ramsey Avenue', 'asynnott4@theatlantic.com', '+62 953 759 8885', 'Bronze', '4MNJB877136');

INSERT INTO flights (id, origin, destination, code, scheduled_dep, scheduled_arr) VALUES (1, 'LBA', 'AMS', '642',  '2021-11-18T06:04:00', '2021-11-18T06:48:00');
INSERT INTO flights (id, origin, destination, code, scheduled_dep, scheduled_arr) VALUES (2, 'LBA', 'LHR', '9607', '2021-11-18T07:36:00', '2021-11-18T08:05:00');
INSERT INTO flights (id, origin, destination, code, scheduled_dep, scheduled_arr) VALUES (3, 'AMS', 'TXL', '7968', '2021-11-18T08:11:00', '2021-11-18T10:41:00');
INSERT INTO flights (id, origin, destination, code, scheduled_dep, scheduled_arr) VALUES (4, 'AMS', 'OSL', '496',  '2021-11-18T11:20:00', '2021-11-18T13:25:00');
INSERT INTO flights (id, origin, destination, code, scheduled_dep, scheduled_arr) VALUES (5, 'LHR', 'JFK', '9230', '2021-11-18T10:36:00', '2021-11-18T19:07:00');

INSERT INTO bookings (id, customer_id, flight_id) VALUES (1,2,1);
INSERT INTO bookings (id, customer_id, flight_id) VALUES (2,1,1);
INSERT INTO bookings (id, customer_id, flight_id) VALUES (3,5,3);
INSERT INTO bookings (id, customer_id, flight_id) VALUES (4,4,2);

-- Wait 10 seconds before inserting the records below

INSERT INTO flight_updates (id, flight_id, updated_dep, reason) VALUES (1, 2, '2021-11-18T09:00:00.000', 'Cabin staff unavailable');
INSERT INTO flight_updates (id, flight_id, updated_dep, reason) VALUES (2, 3, '2021-11-19T14:00:00.000', 'Mechanical checks');
INSERT INTO flight_updates (id, flight_id, updated_dep, reason) VALUES (3, 1, '2021-11-19T08:10:09.000', 'Icy conditions');
