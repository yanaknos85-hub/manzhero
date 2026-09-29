
INSERT INTO tariff_fleet.contractor (id, name, service_type, contractor_type)
VALUES ('a7d2f1b4-fc45-43fe-b5d9-e8b1201e5bc6', 'Подрядчик №1', 'AUTOSERVICE', 'AUTOSERVICE_INTERNAL'),
       ('b1a8d2ce-c3fd-43dd-a15b-d94cfda21a3e', 'Подрядчик №2', 'CARGO_TRANSPORTATION', 'DISPATCHER_EXTERNAL');


INSERT INTO tariff_fleet.contract (id, creation_time, "start", "end", number, creator_user_id, active)
VALUES ('7b6245cc-89a1-43aa-bf15-abf2835e5267', NOW(), '2023-01-01', '2023-12-31', 'Контракт №1', null, true),
       ('153b8956-ae66-4bca-84eb-28a209353a1d', NOW(), '2023-01-01', '2023-12-31', 'Контракт №2', null, true),
       ('968aefdc-300b-4262-be61-ff6a50891b4a', NOW(), '2023-01-01', '2023-12-31', 'Контракт №3', null, true),
       ('d203f316-20fa-41b6-8238-de0644e552b1', NOW(), '2023-01-01', '2023-12-31', 'Контракт №4', null, true);

   INSERT INTO tariff_fleet.organization
   (id, digit_id, active, official_name)
   VALUES('cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 1, false, 'CCC Inc') ON CONFLICT DO NOTHING;


INSERT INTO tariff_fleet.repair_contract (contract_id, contractor_id, organization_id, service_points_name,  amount_without_vat, amount_with_vat)
VALUES ('968aefdc-300b-4262-be61-ff6a50891b4a', 'a7d2f1b4-fc45-43fe-b5d9-e8b1201e5bc6', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 'Подрядчик №1', 50000, 50000),
       ('d203f316-20fa-41b6-8238-de0644e552b1', 'b1a8d2ce-c3fd-43dd-a15b-d94cfda21a3e', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 'Подрядчик №2', 75000, 75000);

INSERT INTO tariff_fleet.fuel_contract (contract_id, contractor_id, service_points_name, amount_without_vat, amount_with_vat, organization_id)
VALUES ('7b6245cc-89a1-43aa-bf15-abf2835e5267', 'a7d2f1b4-fc45-43fe-b5d9-e8b1201e5bc6', 'Подрядчик №1', 100000, 120000, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'),
       ('153b8956-ae66-4bca-84eb-28a209353a1d', 'b1a8d2ce-c3fd-43dd-a15b-d94cfda21a3e', 'Подрядчик №2', 200000, 240000, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6');
