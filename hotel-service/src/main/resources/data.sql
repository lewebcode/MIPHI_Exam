INSERT INTO hotels (id, name, address) VALUES
  (1, 'Demo Hotel', '123 Demo Street');

INSERT INTO rooms (id, hotel_id, number, available, times_booked) VALUES
  (1, 1, '101', true, 0),
  (2, 1, '102', true, 0),
  (3, 1, '103', true, 0);
