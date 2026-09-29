INSERT INTO limits.services (id) VALUES
('REPAIR'),
('PASSENGER'),
('CARGO');

INSERT INTO limits."types" (id) VALUES
	 ('PUBLIC'),
	 ('DOMESTIC_COURIER'),
	 ('CARSHARING'),
	 ('COURIER'),
	 ('SCOOTER'),
	 ('DEDICATED'),
	 ('BICYCLE'),
	 ('OFFICIAL'),
	 ('SPECIAL'),
	 ('INDIVIDUAL'),
	 ('PRIVATE'),
	 ('TAXI'),
	 ('INTERREGIONAL'),
	 ('PERSONAL'),
	 ('WALK');


insert
	into
	limits."limit" (id,
	human_readable_id,
	author_id,
	limit_owner_id,
	limit_status,
	"year",
	limit_type,
	limit_sharing_type,
	"service_type",
	sum,
	reserve,
	final_sharing,
	use_my_limit,
	creation_time,
	parent_id,
	organization_id,
	department_id,
	employee_id,
	parent_department_id,
	economy,
	update_time,
	hash)
values
('9f236cc6-d00a-4eea-acf3-07dc23bfe2ed',
'LD-5437-00000001',
'15BDC75D-533B-44BB-9539-3DDC8693F3D3',
'15BDC75D-533B-44BB-9539-3DDC8693F3D3',
'SHARED'::limits."status",
(select extract(year from CURRENT_DATE)),
'DEPARTMENT'::limits."type",
'MONTHLY'::limits."sharing_type",
'PASSENGER',
1000000.00,
600000.00,
false,
true,
'2025-05-26 11:00:39.151',
null,--parent_id
'621c288d-e348-46e5-a319-cbf61ef1e396',--organization_id
'489A0090-1819-4C60-A611-572EA115C6A4',--department_id
null,
null,--parent_department_id
0.00,
'2025-10-23 11:46:18.318',
'4400634151550335253'),
('585df74d-c5e1-4452-9fcf-8e2202bb8106',
'LD-5437-00000002',
'15BDC75D-533B-44BB-9539-3DDC8693F3D3',
'15BDC75D-533B-44BB-9539-3DDC8693F3D3',
'SHARED'::limits."status",
(select extract(year from CURRENT_DATE)),
'DEPARTMENT'::limits."type",
'MONTHLY'::limits."sharing_type",
'PASSENGER',
1000000.00,
600000.00,
false,
true,
'2025-05-26 11:00:39.151',
'9f236cc6-d00a-4eea-acf3-07dc23bfe2ed',--parent_id
'621c288d-e348-46e5-a319-cbf61ef1e396',--organization_id
'c3820eb5-e6a5-4fb6-a5c9-1e88a0e5055d',--department_id
null,
'489A0090-1819-4C60-A611-572EA115C6A4',--parent_department_id
0.00,
'2025-10-23 11:46:18.318',
'4400634151550335253'),
('0c21f83d-c417-4b07-98e9-fec8a4f2495c',
'LD-5437-00000002',
'15BDC75D-533B-44BB-9539-3DDC8693F3D3',
'15BDC75D-533B-44BB-9539-3DDC8693F3D3',
'SHARED'::limits."status",
(select extract(year from CURRENT_DATE)),
'DEPARTMENT'::limits."type",
'MONTHLY'::limits."sharing_type",
'PASSENGER',
1000000.00,
600000.00,
false,
true,
'2025-05-26 11:00:39.151',
'585df74d-c5e1-4452-9fcf-8e2202bb8106',--parent_id
'621c288d-e348-46e5-a319-cbf61ef1e396',--organization_id
'79fb1427-6e87-4755-bdde-fb3434d1bbd2',--department_id
null,
'c3820eb5-e6a5-4fb6-a5c9-1e88a0e5055d',--parent_department_id
0.00,
'2025-10-23 11:46:18.318',
'4400634151550335253'),
('d4390f6a-4f45-4b69-8553-a9cc6d26bf1a',
'LD-5437-00000003',
'15BDC75D-533B-44BB-9539-3DDC8693F3D3',
'15BDC75D-533B-44BB-9539-3DDC8693F3D3',
'SHARED'::limits."status",
(select extract(year from CURRENT_DATE)),
'DEPARTMENT'::limits."type",
'MONTHLY'::limits."sharing_type",
'PASSENGER',
1000000.00,
600000.00,
false,
true,
'2025-05-26 11:00:39.151',
null,--parent_id
'11501599-b498-4e9c-8b75-3e5899c445c0',--organization_id
'e4cdf1e8-45d6-4ecf-8330-6b10760d256d',--department_id
null,
null,--parent_department_id
0.00,
'2025-10-23 11:46:18.318',
'4400634151550335253');

INSERT INTO limits.sharings (id,author_id,creation_time,sum,remains,transport_type,limit_id,distributed) VALUES
	 ('6d61b01a-5395-448e-95ee-bc120bc61973'::uuid,'15BDC75D-533B-44BB-9539-3DDC8693F3D3'::uuid,'2025-11-12 15:50:55.996',90000000.00,90000000.00,'CARSHARING','9f236cc6-d00a-4eea-acf3-07dc23bfe2ed'::uuid,true),
	 ('e8485b2c-313e-400b-8c8c-8df56c0f204c'::uuid,'15BDC75D-533B-44BB-9539-3DDC8693F3D3'::uuid,'2025-11-12 15:50:56.097',90000000.00,90000000.00,'PERSONAL','9f236cc6-d00a-4eea-acf3-07dc23bfe2ed'::uuid,true),
	 ('71148283-e8cc-4c59-abed-44690309ecdc'::uuid,'15BDC75D-533B-44BB-9539-3DDC8693F3D3'::uuid,'2025-11-12 15:50:56.199',90000000.00,90000000.00,'PUBLIC','9f236cc6-d00a-4eea-acf3-07dc23bfe2ed'::uuid,true),
	 ('edaddfe8-67f1-4db6-bb51-9e993fce78cd'::uuid,'15BDC75D-533B-44BB-9539-3DDC8693F3D3'::uuid,'2025-11-12 15:50:56.298',90000000.00,90000000.00,'TAXI','9f236cc6-d00a-4eea-acf3-07dc23bfe2ed'::uuid,true),
	 ('96d2eeb7-a54f-478f-8497-71648551c872'::uuid,'15BDC75D-533B-44BB-9539-3DDC8693F3D3'::uuid,'2025-11-12 15:51:23.204',9999999.00,9999999.00,'CARSHARING','585df74d-c5e1-4452-9fcf-8e2202bb8106'::uuid,true),
	 ('b3b87af7-c276-47bf-98e6-141c2a692083'::uuid,'15BDC75D-533B-44BB-9539-3DDC8693F3D3'::uuid,'2025-11-12 15:51:23.293',9999999.00,9999999.00,'PERSONAL','585df74d-c5e1-4452-9fcf-8e2202bb8106'::uuid,true),
	 ('8a3d7a85-ecb8-4096-b3b6-128eb76aa53c'::uuid,'15BDC75D-533B-44BB-9539-3DDC8693F3D3'::uuid,'2025-11-12 15:51:23.381',9999999.00,9999999.00,'PUBLIC','585df74d-c5e1-4452-9fcf-8e2202bb8106'::uuid,true),
	 ('56942341-6dd0-4b0c-b2bc-b5469535a34d'::uuid,'15BDC75D-533B-44BB-9539-3DDC8693F3D3'::uuid,'2025-11-12 15:51:23.411',9999999.00,9999999.00,'TAXI','585df74d-c5e1-4452-9fcf-8e2202bb8106'::uuid,true),
	 ('29c52861-4ecb-4f55-b2b6-5397fa5e788c'::uuid,'15BDC75D-533B-44BB-9539-3DDC8693F3D3'::uuid,'2025-11-12 15:51:23.204',9999999.00,9999999.00,'CARSHARING','0c21f83d-c417-4b07-98e9-fec8a4f2495c'::uuid,true),
	 ('f5150fdb-e2cd-4c58-92a1-23a2512b6228'::uuid,'15BDC75D-533B-44BB-9539-3DDC8693F3D3'::uuid,'2025-11-12 15:51:23.293',9999999.00,9999999.00,'PERSONAL','0c21f83d-c417-4b07-98e9-fec8a4f2495c'::uuid,true),
	 ('1516d2e9-8c77-4bd9-b179-b8353fa31588'::uuid,'15BDC75D-533B-44BB-9539-3DDC8693F3D3'::uuid,'2025-11-12 15:51:23.381',9999999.00,9999999.00,'PUBLIC','0c21f83d-c417-4b07-98e9-fec8a4f2495c'::uuid,true),
	 ('b7e659d6-eeb1-4c49-a2be-eb30b02b679c'::uuid,'15BDC75D-533B-44BB-9539-3DDC8693F3D3'::uuid,'2025-11-12 15:51:23.411',9999999.00,9999999.00,'TAXI','0c21f83d-c417-4b07-98e9-fec8a4f2495c'::uuid,true);