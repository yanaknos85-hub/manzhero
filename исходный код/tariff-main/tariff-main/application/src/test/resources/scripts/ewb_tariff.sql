insert into tariff_fleet.tariff (id, contract_id, active, activation_type, human_readable_id, creator_user_id, creation_time)
VALUES ('3c2d9e7a-cc85-7b05-8692-263cb759408d', '5627f0b0-cac0-49e2-be7f-db026fa42435', true, 'AUTO', 'TF-0008-00000001',
        '3cd35c19-fd39-413c-99a0-30f35bd642a8', '2025-03-15 07:16:01.446854'),
       ('fbd0afa4-0c43-165f-404a-55f14ed78306', '20bfb1f4-6099-45e8-8d81-c42df5070763', false, 'AUTO', 'TF-0008-00000002',
        '3cd35c19-fd39-413c-99a0-30f35bd642a8', '2024-04-15 07:30:05.970180'),
       ('6f3350fd-1c55-19ab-e861-09ccfd6f34f7', 'e921aa12-0668-4bac-856c-c3d851882911', true, 'AUTO', 'TF-0008-00000003',
        '7dd56ea0-fa38-400d-93a6-2a4ef4b7df70', '2024-04-15 07:30:51.187395');

insert into tariff_fleet.ewb_tariff (tariff_id, organization_id, department_id, amount)
VALUES ('3c2d9e7a-cc85-7b05-8692-263cb759408d', '621c288d-e348-46e5-a319-cbf61ef1e396', '489A0090-1819-4C60-A611-572EA115C6A4', 3333),
       ('fbd0afa4-0c43-165f-404a-55f14ed78306', '621c288d-e348-46e5-a319-cbf61ef1e396', '489A0090-1819-4C60-A611-572EA115C6A4', 9999),
       ('6f3350fd-1c55-19ab-e861-09ccfd6f34f7', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '482e6dcb-03a9-4927-90b4-c7081114a9d8', 99999);