insert into tariff_fleet.contract (id, creation_time, start, "end", number, uvhd, creator_user_id, active)
values  ('5627f0b0-cac0-49e2-be7f-db026fa42435', '2025-03-15 07:16:01.446854', '2025-03-15', current_date + interval '1 year', '11111', null,
         '3cd35c19-fd39-413c-99a0-30f35bd642a8', true),
        ('20bfb1f4-6099-45e8-8d81-c42df5070763', '2024-04-15 07:30:05.970180', '2024-04-15', '2024-05-15', '99999', null,
         '3cd35c19-fd39-413c-99a0-30f35bd642a8', false),
        ('e921aa12-0668-4bac-856c-c3d851882911', '2024-04-15 07:30:51.187395', '2024-04-15', current_date + interval '2 year', '9999', null,
         '7dd56ea0-fa38-400d-93a6-2a4ef4b7df70', true),
        ('80cea1a8-b869-49d0-b0dd-de0079fbb134', '2025-07-10 07:05:11.456987', current_date - interval '1 day', current_date + interval '1 day', '9999', null,
         '7dd56ea0-fa38-400d-93a6-2a4ef4b7df70', false),
        ('be8ba3fc-5064-4ad9-9972-7aa33079516d', '2025-07-10 07:05:11.324675', current_date - interval '2 day', current_date - interval '1 day', '9999', null,
         '7dd56ea0-fa38-400d-93a6-2a4ef4b7df70', true);

insert into tariff_fleet.organization_medical_license (id, series, number, issue_date, expiry_date)
values  ('d8586e9a-a50c-4c6f-a581-e01ef501e983', '55555', '666666', '2025-03-15', '2025-06-15'),
        ('ed82f1ee-6b36-429c-8e6f-37eb0e8b3775', '99999', '999999', '2024-04-15', '2024-05-15'),
        ('2a490d36-75af-43e6-8ea6-746cb783f843', '999999', '9999999', '2024-04-15', '2027-05-15');


insert into tariff_fleet.ewb_contract (contract_id, organization_id, amount, inspection_type, edf_operator_id, edf_code, organization_medical_license_id)
values  ('5627f0b0-cac0-49e2-be7f-db026fa42435', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 33333, 'MEDIC', '2AE', '444444', 'd8586e9a-a50c-4c6f-a581-e01ef501e983'),
        ('20bfb1f4-6099-45e8-8d81-c42df5070763', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 99999, 'MEDIC', '2BM', '99999', 'ed82f1ee-6b36-429c-8e6f-37eb0e8b3775'),
        ('e921aa12-0668-4bac-856c-c3d851882911', '621c288d-e348-46e5-a319-cbf61ef1e396', 999999, 'TECHNIC', '2BM', '999999', '2a490d36-75af-43e6-8ea6-746cb783f843'),
        ('80cea1a8-b869-49d0-b0dd-de0079fbb134', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 99999, 'MEDIC', '2BM', '99999', 'ed82f1ee-6b36-429c-8e6f-37eb0e8b3775'),
        ('be8ba3fc-5064-4ad9-9972-7aa33079516d', '621c288d-e348-46e5-a319-cbf61ef1e396', 999999, 'TECHNIC', '2BM', '999999', '2a490d36-75af-43e6-8ea6-746cb783f843');