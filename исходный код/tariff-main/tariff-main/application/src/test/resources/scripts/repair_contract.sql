insert into tariff_fleet.contract (id, creation_time, start, "end", number, uvhd, creator_user_id, active)
values  ('999d5145-73e0-4518-aba2-bf9b3ea81088', '2025-03-15 07:16:01.446854', current_date - interval '3 day', current_date + interval '3 day', '11111', '1',
         '3cd35c19-fd39-413c-99a0-30f35bd642a8', true),
        ('5805f22f-30e3-440f-91b4-4b5fe2c28405', '2024-04-15 07:30:05.970180', current_date - interval '10 day', current_date - interval '9 day', '99999', '22',
         '3cd35c19-fd39-413c-99a0-30f35bd642a8', false),
        ('5c7b86b9-667f-4fb9-8d2e-ab3e871751eb', '2024-04-15 07:30:51.187395', current_date - interval '10 day', current_date + interval '10 day', '9999', '333',
         '7dd56ea0-fa38-400d-93a6-2a4ef4b7df70', true);

insert into tariff_fleet.repair_contract (contract_id, contractor_id, amount_without_vat, amount_with_vat, service_points_name, organization_id, logo_s3_id)
values  ('999d5145-73e0-4518-aba2-bf9b3ea81088', 'a033de41-2228-40ba-bc13-e97849625484', 33333, 44444,'Контрагент 1',
         'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6',null),
        ('5805f22f-30e3-440f-91b4-4b5fe2c28405', 'a033de41-2228-40ba-bc13-e97849625484', 99999, 10000,'Контрагент 1',
         'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6',null),
        ('5c7b86b9-667f-4fb9-8d2e-ab3e871751eb', 'b9ec53c0-be74-4aac-b295-873083c7ce4d', 999999, 10000000, 'Контрагент 2',
         'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '7e0047ad-0877-4567-b3bf-552194416b1e');
