
INSERT INTO tariff_fleet.contractor (id, name, active)
VALUES ('b1a8d2ce-c3fd-43dd-a15b-d94cfda21a3e', 'Контрактор №1', TRUE);

INSERT INTO tariff_fleet.contract (id, creation_time, "start", "end", number, uvhd, creator_user_id, active)
VALUES
('7b6245cc-89a1-43aa-bf15-abf2835e5267', NOW(), CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 'FUEL_CONTRACT_001', NULL, '3cd35c19-fd39-413c-99a0-30f35bd642a8', TRUE),
('7b6245cc-89a1-43aa-bf15-abf2835e5268', NOW(), CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 'FUEL_CONTRACT_002', NULL, '3cd35c19-fd39-413c-99a0-30f35bd642a8', TRUE),
('7b6245cc-89a1-43aa-bf15-abf2835e5269', NOW(), CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 'REPAIR_CONTRACT_001', NULL, '3cd35c19-fd39-413c-99a0-30f35bd642a8', TRUE),
('153b8956-ae66-4bca-84eb-28a209353a1d', NOW(), CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 'FUEL_CONTRACT_003', NULL, '3cd35c19-fd39-413c-99a0-30f35bd642a8', TRUE),
('153b8956-ae66-4bca-84eb-28a209353a1e', NOW(), CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 'REPAIR_CONTRACT_002', NULL, '3cd35c19-fd39-413c-99a0-30f35bd642a8', TRUE),
('153b8956-ae66-4bca-84eb-28a209353a1f', NOW(), CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 'REPAIR_CONTRACT_003', NULL, '3cd35c19-fd39-413c-99a0-30f35bd642a8', TRUE);

INSERT INTO tariff_fleet.fuel_contract (contract_id, contractor_id, service_points_name, amount_without_vat, amount_with_vat, organization_id, logo_s3_id)
VALUES ('7b6245cc-89a1-43aa-bf15-abf2835e5267', 'b1a8d2ce-c3fd-43dd-a15b-d94cfda21a3e', 'Подрядчик №11', 100000, 120000, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '7b6245cc-89a1-43aa-bf15-abf2835e5267'),
       ('153b8956-ae66-4bca-84eb-28a209353a1d', 'b1a8d2ce-c3fd-43dd-a15b-d94cfda21a3e', 'Подрядчик №21', 200000, 240000, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '153b8956-ae66-4bca-84eb-28a209353a1d'),
       ('7b6245cc-89a1-43aa-bf15-abf2835e5269', 'b1a8d2ce-c3fd-43dd-a15b-d94cfda21a3e', 'Подрядчик №21', 200000, 240000, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '153b8956-ae66-4bca-84eb-28a209353a1d');


INSERT INTO tariff_fleet.repair_contract (contract_id, contractor_id, service_points_name, amount_without_vat,  amount_with_vat, organization_id, logo_s3_id)
VALUES ('7b6245cc-89a1-43aa-bf15-abf2835e5268', 'b1a8d2ce-c3fd-43dd-a15b-d94cfda21a3e', 'Подрядчик №12', 100000, 120000, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '7b6245cc-89a1-43aa-bf15-abf2835e5268'),
       ('153b8956-ae66-4bca-84eb-28a209353a1e', 'b1a8d2ce-c3fd-43dd-a15b-d94cfda21a3e', 'Подрядчик №22', 200000, 240000, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '153b8956-ae66-4bca-84eb-28a209353a1e'),
       ('153b8956-ae66-4bca-84eb-28a209353a1f', 'b1a8d2ce-c3fd-43dd-a15b-d94cfda21a3e', 'Подрядчик №22', 200000, 240000, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '153b8956-ae66-4bca-84eb-28a209353a1e');

INSERT INTO tariff_fleet.tariff (id, contract_id, active, activation_type, human_readable_id, creation_time, creator_user_id)
VALUES ('7b6245cc-89a1-43aa-bf15-abf2835e5267', '7b6245cc-89a1-43aa-bf15-abf2835e5267', true, 'MANUAL', 'TARIFF_001', NOW(), '3cd35c19-fd39-413c-99a0-30f35bd642a8'),
       ('7b6245cc-89a1-43aa-bf15-abf2835e5268', '7b6245cc-89a1-43aa-bf15-abf2835e5268', true, 'MANUAL', 'TARIFF_002', NOW(), '3cd35c19-fd39-413c-99a0-30f35bd642a8'),
       ('153b8956-ae66-4bca-84eb-28a209353a1d', '153b8956-ae66-4bca-84eb-28a209353a1d', true, 'MANUAL', 'TARIFF_003', NOW(), '3cd35c19-fd39-413c-99a0-30f35bd642a8'),
       ('153b8956-ae66-4bca-84eb-28a209353a1e', '153b8956-ae66-4bca-84eb-28a209353a1e', true, 'MANUAL', 'TARIFF_004', NOW(), '3cd35c19-fd39-413c-99a0-30f35bd642a8');

INSERT INTO tariff_fleet.repair_tariff (tariff_id, organization_id, hour_normalized_price, detail_discount_price, work_warranty, mileage_warranty, detail_warranty, is_field_service, department_id)
VALUES ('7b6245cc-89a1-43aa-bf15-abf2835e5268', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 10000, 1, 1, 1, 1, true, '482e6dcb-03a9-4927-90b4-c7081114a9d8'),
       ('153b8956-ae66-4bca-84eb-28a209353a1e', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 10001, 2, 2, 2, 2, true, '482e6dcb-03a9-4927-90b4-c7081114a9d8');

INSERT INTO tariff_fleet.fuel_tariff (tariff_id, organization_id, department_id, discount)
VALUES ('153b8956-ae66-4bca-84eb-28a209353a1d', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '482e6dcb-03a9-4927-90b4-c7081114a9d8', 100),
       ('7b6245cc-89a1-43aa-bf15-abf2835e5267', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '482e6dcb-03a9-4927-90b4-c7081114a9d8', 0);