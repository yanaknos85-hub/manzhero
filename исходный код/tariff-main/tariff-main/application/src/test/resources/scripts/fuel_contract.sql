insert into tariff_fleet.contract (id, creation_time, start, "end", number, uvhd, creator_user_id, active)
values  ('780916d0-b750-4e54-abe3-c253885fffc1', '2025-03-15 07:16:01.446854', current_date - interval '3 day', current_date + interval '3 day', '11111', '1',
         '3cd35c19-fd39-413c-99a0-30f35bd642a8', true),
        ('afbf3a73-9a6f-4729-8769-4dad02348867', '2024-04-15 07:30:05.970180', current_date - interval '10 day', current_date - interval '9 day', '99999', '22',
         '3cd35c19-fd39-413c-99a0-30f35bd642a8', false),
        ('6198e89f-d00d-415c-9e95-b9fe49074038', '2024-04-15 07:30:51.187395', current_date - interval '10 day', current_date + interval '10 day', '9999', '333',
         '7dd56ea0-fa38-400d-93a6-2a4ef4b7df70', true);

insert into tariff_fleet.fuel_contract (contract_id, contractor_id, amount_without_vat, amount_with_vat, service_points_name, organization_id, logo_s3_id)
values  ('780916d0-b750-4e54-abe3-c253885fffc1', 'a033de41-2228-40ba-bc13-e97849625484', 33333, 44444,'Контрагент 1',
         'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6',null),
        ('afbf3a73-9a6f-4729-8769-4dad02348867', 'a033de41-2228-40ba-bc13-e97849625484', 99999, 10000,'Контрагент 1',
         'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6',null),
        ('6198e89f-d00d-415c-9e95-b9fe49074038', 'b9ec53c0-be74-4aac-b295-873083c7ce4d', 999999, 10000000, 'Контрагент 2',
         'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '7e0047ad-0877-4567-b3bf-552194416b1e');

insert into tariff_fleet.service_point (id, address, latitude, longitude, contract_id, active)
values  ('11111111-1111-1111-1111-111111111111', 'Москва, ул. Пушкина, д. 1', 55.7558, 37.6173, '780916d0-b750-4e54-abe3-c253885fffc1', true),
        ('22222222-2222-2222-2222-222222222222', 'Москва, ул. Ленина, д. 2', 55.7568, 37.6183, '780916d0-b750-4e54-abe3-c253885fffc1', true),
        ('33333333-3333-3333-3333-333333333333', 'Санкт-Петербург, Невский проспект, д. 5', 59.9311, 30.3609, 'afbf3a73-9a6f-4729-8769-4dad02348867', false);
