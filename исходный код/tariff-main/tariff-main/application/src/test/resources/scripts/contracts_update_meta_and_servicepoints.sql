INSERT INTO tariff_fleet.contractor (id, name, active, contractor_type)
VALUES ('b1a8d2ce-c3fd-43dd-a15b-d94cfda21a3e', 'Контрактор №1', TRUE, 'OFFLINE');

INSERT INTO tariff_fleet.contract (id, creation_time, "start", "end", number, uvhd, creator_user_id, active)
VALUES
('7b6245cc-89a1-43aa-bf15-abf2835e5267', NOW(), CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 'FUEL_CONTRACT_001', NULL, NULL, TRUE),
('7b6245cc-89a1-43aa-bf15-abf2835e5268', NOW(), CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 'FUEL_CONTRACT_002', NULL, NULL, TRUE),
('153b8956-ae66-4bca-84eb-28a209353a1d', NOW(), CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 'REPAIR_CONTRACT_001', NULL, NULL, TRUE),
('153b8956-ae66-4bca-84eb-28a209353a1e', NOW(), CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 'REPAIR_CONTRACT_002', NULL, NULL, TRUE);


INSERT INTO tariff_fleet.fuel_contract (contract_id, contractor_id, service_points_name, amount_without_vat, amount_with_vat, organization_id, logo_s3_id)
VALUES ('7b6245cc-89a1-43aa-bf15-abf2835e5267', 'b1a8d2ce-c3fd-43dd-a15b-d94cfda21a3e', 'Подрядчик №11', 100000, 120000, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '7b6245cc-89a1-43aa-bf15-abf2835e5267'),
       ('153b8956-ae66-4bca-84eb-28a209353a1d', 'b1a8d2ce-c3fd-43dd-a15b-d94cfda21a3e', 'Подрядчик №21', 200000, 240000, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '153b8956-ae66-4bca-84eb-28a209353a1d');


INSERT INTO tariff_fleet.repair_contract (contract_id, contractor_id, service_points_name, amount_without_vat,  amount_with_vat, organization_id, logo_s3_id)
VALUES ('7b6245cc-89a1-43aa-bf15-abf2835e5268', 'b1a8d2ce-c3fd-43dd-a15b-d94cfda21a3e', 'Подрядчик №12', 100000, 120000, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '7b6245cc-89a1-43aa-bf15-abf2835e5268'),
       ('153b8956-ae66-4bca-84eb-28a209353a1e', 'b1a8d2ce-c3fd-43dd-a15b-d94cfda21a3e', 'Подрядчик №22', 200000, 240000, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '153b8956-ae66-4bca-84eb-28a209353a1e');


INSERT INTO tariff_fleet.service_point (id, address, latitude, longitude, contract_id)
VALUES
('7b6245cc-89a1-43aa-bf15-abf2835e5267', 'Москва, ул. Тверская, д. 1', 55.7558, 37.6173, '7b6245cc-89a1-43aa-bf15-abf2835e5267'),
('7b6245cc-89a1-43aa-bf15-abf2835e5268', 'Санкт-Петербург, Невский проспект, д. 1', 59.9386, 30.3141, '153b8956-ae66-4bca-84eb-28a209353a1d'),
('7b6245cc-89a1-43aa-bf15-abf2835e5269', 'Москва, ул. Тверская, д. 2', 55.7558, 37.6173, '7b6245cc-89a1-43aa-bf15-abf2835e5267'),
('7b6245cc-89a1-43aa-bf15-abf2835e5270', 'Санкт-Петербург, Невский проспект, д. 3', 59.9386, 30.3141, '153b8956-ae66-4bca-84eb-28a209353a1d');
