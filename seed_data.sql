-- Seed data for ROSTRY Platform MVP

-- Insert sample users with different roles and KYC states
INSERT INTO users (id, email, phone, password_hash, role, kyc_state, first_name, last_name, location) VALUES
('11111111-1111-1111-1111-111111111111', 'admin@example.com', '+1234567890', 'hashed_password_1', 'admin', 'verified', 'Admin', 'User', '{"lat": 17.3850, "lng": 78.4867, "address": "Hyderabad, Telangana"}'),
('22222222-2222-2222-2222-222222222222', 'farmer@example.com', '+1234567891', 'hashed_password_2', 'farmer', 'verified', 'Farmer', 'User', '{"lat": 16.5062, "lng": 80.6489, "address": "Vijayawada, Andhra Pradesh"}'),
('33333333-3333-3333-3333-333333333333', 'breeder@example.com', '+1234567892', 'hashed_password_3', 'breeder', 'verified', 'Breeder', 'User', '{"lat": 17.6868, "lng": 83.2185, "address": "Visakhapatnam, Andhra Pradesh"}'),
('44444444-4444-4444-4444-444444444444', 'general@example.com', '+1234567893', 'hashed_password_4', 'general', 'unverified', 'General', 'User', '{"lat": 17.3850, "lng": 78.4867, "address": "Hyderabad, Telangana"}');

-- Insert sample fowls with parent links
INSERT INTO fowls (id, owner_id, breed, gender, date_of_birth, parent_ids, breeder_ready) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', 'Aseel', 'male', '2023-01-15', ARRAY['cccccccc-cccc-cccc-cccc-cccccccccccc', 'dddddddd-dddd-dddd-dddd-dddddddddddd'], TRUE),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', 'Kadaknath', 'female', '2023-03-22', ARRAY['cccccccc-cccc-cccc-cccc-cccccccccccc', 'dddddddd-dddd-dddd-dddd-dddddddddddd'], FALSE),
('cccccccc-cccc-cccc-cccc-cccccccccccc', '33333333-3333-3333-3333-333333333333', 'Aseel', 'male', '2020-05-10', NULL, TRUE),
('dddddddd-dddd-dddd-dddd-dddddddddddd', '33333333-3333-3333-3333-333333333333', 'Kadaknath', 'female', '2020-07-18', NULL, TRUE);

-- Insert sample fowl records
INSERT INTO fowl_records (id, fowl_id, record_type, details, proof_urls) VALUES
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'vaccination', '{"vaccine_type": "Newcastle", "date": "2023-02-15"}', ARRAY['https://storage.example.com/vaccination_proof_1.jpg']),
('ffffffff-ffff-ffff-ffff-ffffffffffff', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '5-week', '{"weight_kg": 1.2, "color": "black"}', ARRAY['https://storage.example.com/5week_proof_1.jpg']),
('00000000-0000-0000-0000-000000000000', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'vaccination', '{"vaccine_type": "Fowlpox", "date": "2023-04-22"}', ARRAY['https://storage.example.com/vaccination_proof_2.jpg']);

-- Insert sample marketplace listing
INSERT INTO market_listings (id, fowl_id, seller_id, purpose, price_cents, location, status) VALUES
('12345678-1234-1234-1234-123456789012', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', 'breeding', 150000, '{"lat": 16.5062, "lng": 80.6489, "address": "Vijayawada, Andhra Pradesh"}', 'active');

-- Insert sample pending transfer
INSERT INTO transfer_logs (id, fowl_id, giver_id, receiver_id, status, verification_details) VALUES
('87654321-4321-4321-4321-210987654321', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', 'pending', NULL);

-- Insert sample messages
INSERT INTO messages (id, sender_id, receiver_id, thread_id, content) VALUES
('11111111-2222-3333-4444-555555555555', '22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', '99999999-8888-7777-6666-555555555555', 'Hi, I am interested in your rooster listed for breeding.'),
('22222222-3333-4444-5555-666666666666', '33333333-3333-3333-3333-333333333333', '22222222-2222-2222-2222-222222222222', '99999999-8888-7777-6666-555555555555', 'Great! Can you share more details about your requirements?');

-- Insert sample coin ledger entries
INSERT INTO coin_ledger (id, user_id, amount, transaction_type, related_entity_id) VALUES
('11111111-3333-5555-7777-999999999999', '22222222-2222-2222-2222-222222222222', -1, 'listing', '12345678-1234-1234-1234-123456789012'),
('22222222-4444-6666-8888-000000000000', '22222222-2222-2222-2222-222222222222', 10, 'admin_credit', NULL);