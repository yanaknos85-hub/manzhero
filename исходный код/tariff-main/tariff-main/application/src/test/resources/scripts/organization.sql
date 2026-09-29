-- ORGANIZATION 1
INSERT INTO tariff_fleet.organization
(id, digit_id, active, official_name)
VALUES('c93b30b8-4a35-4c29-8d92-2fb3279a3841', 1, false, 'CCC Inc') ON CONFLICT DO NOTHING;

-- ORGANIZATION 2
INSERT INTO tariff_fleet.organization
(id, digit_id, active, official_name)
VALUES('d41d56ab-6e33-4977-89be-5a3eaf5e72a3', 2, true, 'BBB Inc') ON CONFLICT DO NOTHING;

-- ORGANIZATION 3
INSERT INTO tariff_fleet.organization
(id, digit_id, active, official_name)
VALUES('46a3b980-d801-499b-87eb-be0bf3f2de53', 3, true, 'AAA Inc') ON CONFLICT DO NOTHING;