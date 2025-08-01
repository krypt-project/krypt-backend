-- Database Initialisation
-- User
INSERT INTO user_table(last_name, first_name, email, password, creation_date, modification_date, email_verified) VALUES
('Durand', 'Alice', 'alice.durand@example.com', '$2y$10$eqjReaDGnKGfJbGHGib7te.mCZoaLDmcby8GYw1jlIpVoLZykM4kq', '2025-08-01T10:00:00', '2025-08-01T10:00:00', true),
('Martin', 'Bob', 'bob.martin@example.com', '$2y$10$eqjReaDGnKGfJbGHGib7te.mCZoaLDmcby8GYw1jlIpVoLZykM4kq', '2025-08-01T11:00:00', '2025-08-01T11:00:00', false),
('Lemoine', 'Claire', 'claire.lemoine@example.com', '$2y$10$eqjReaDGnKGfJbGHGib7te.mCZoaLDmcby8GYw1jlIpVoLZykM4kq', '2025-08-01T12:00:00', '2025-08-01T12:00:00', true);