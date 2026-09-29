-- ORGANIZATION_1
INSERT INTO corporate.organization (id, digit_id, official_name, address, "status", msrn, tin, sync_id, organization_code, organization_group_id)
VALUES ('cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 8, 'ЦА', 'Уфа Жукова 11', 'ACTIVE', '182828394949493', '678756873452', '288238388383', NULL, NULL);
-- DEPARTMENT_1
INSERT INTO corporate.department (id, "status", "name", humanreadableid, organization_id, parent_id, code, "location", head, level_code, level_name, geozone, org_structure_type, is_handmade, update_time)
VALUES ('482e6dcb-03a9-4927-90b4-c7081114a9d8', 'ACTIVE', 'ПАО «Сбербанк России» (ЦА)', 'DT-0008-00000161', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', null, '3', 'Мурманская область', null, 0, null, null, 'EXTERNAL', true, '2025-04-28 11:52:00.914'
);
-- POSITION_1
INSERT INTO corporate."position" (id,"name",self_approved,organization_id,humanreadableid,org_structure_type,sync_id,"status",update_time) VALUES
	 ('69a4d3f0-223b-46f5-8fed-0e856a1889dc'::uuid,'Планктон',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'PS-0009-00000001','EXTERNAL'::corporate."structure_type",NULL,'ACTIVE'::corporate."active_status",'2025-01-23 17:26:02.633');
-- PERSON_1
INSERT INTO corporate.employee (id,email,first_name,last_name,mobile_phone,patronymic,personnel_number,department_id,position_id,supervisor_id,user_id,"status",humanreadableid,"gender",fire_date,external_email,room,consent,cost_center,itinerant_type,marriage_certificate_id,creation_time,org_structure_type,organization_id,"_need_update",update_time,phone_confirmed) VALUES
	 ('3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'sbertransport1_test_test@test1-exchtest.sbrf.ru','Петр','Петров','+73123123123',null,'2016497','482e6dcb-03a9-4927-90b4-c7081114a9d8'::uuid,
	 '69a4d3f0-223b-46f5-8fed-0e856a1889dc'::uuid,NULL,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'ACTIVE'::corporate."active_status",'US-0008-00044610',NULL,NULL,NULL,NULL,true,'9900L01120','NONE'::corporate."itinerant",NULL,'2022-10-17 11:57:56.301','EXTERNAL'::corporate."structure_type",'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,false,'2025-11-10 16:15:16.791',true);
--PERSON_3
INSERT INTO corporate.employee (id,email,first_name,last_name,mobile_phone,patronymic,personnel_number,department_id,position_id,supervisor_id,user_id,"status",humanreadableid,"gender",fire_date,external_email,room,consent,cost_center,itinerant_type,marriage_certificate_id,creation_time,org_structure_type,organization_id,"_need_update",update_time,phone_confirmed) VALUES
	 ('7dd56ea0-fa38-400d-93a6-2a4ef4b7df70'::uuid,'sbertransport1_test_test@test3-exchtest.sbrf.ru','Александр','Александров','+73123123123','Александрович','3016497','482e6dcb-03a9-4927-90b4-c7081114a9d8'::uuid,
	 '69a4d3f0-223b-46f5-8fed-0e856a1889dc'::uuid,NULL,'7dd56ea0-fa38-400d-93a6-2a4ef4b7df70'::uuid,'ACTIVE'::corporate."active_status",'US-0008-00044611',NULL,NULL,NULL,NULL,true,'9900L01121','NONE'::corporate."itinerant",NULL,'2022-10-17 11:57:56.301','EXTERNAL'::corporate."structure_type",'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,false,'2025-11-10 16:15:16.791',true);

-- ORGANIZATION_2
INSERT INTO corporate.organization (id, digit_id, official_name, address, "status", msrn, tin, sync_id, organization_code, organization_group_id)
VALUES ('621c288d-e348-46e5-a319-cbf61ef1e396', 11, 'Тест2', 'Караганда улица Ленина 1', 'ACTIVE', '182823851949493', '436543745754', '8931538388383', NULL, NULL);
-- DEPARTMENT_2
INSERT INTO corporate.department (id, "status", "name", humanreadableid, organization_id, parent_id, code, "location", head, level_code, level_name, geozone, org_structure_type, is_handmade, update_time)
VALUES ('489A0090-1819-4C60-A611-572EA115C6A4', 'ACTIVE', 'Test2', 'DT-0008-00000222', '621c288d-e348-46e5-a319-cbf61ef1e396', null, '4', 'Костромская область', null, 0, null, null, 'EXTERNAL', true, '2025-04-28 11:52:00.914'
);
-- POSITION_2
INSERT INTO corporate."position" (id,"name",self_approved,organization_id,humanreadableid,org_structure_type,sync_id,"status",update_time) VALUES
	 ('3908803B-A08C-4D96-AA71-9DD5A6D3ABD4'::uuid,'Планктон',true,'621c288d-e348-46e5-a319-cbf61ef1e396'::uuid,'PS-0009-00000002','EXTERNAL'::corporate."structure_type",NULL,'ACTIVE'::corporate."active_status",'2025-01-23 17:26:02.633');
-- PERSON_2
INSERT INTO corporate.employee (id,email,first_name,last_name,mobile_phone,patronymic,personnel_number,department_id,position_id,supervisor_id,user_id,"status",humanreadableid,"gender",fire_date,external_email,room,consent,cost_center,itinerant_type,marriage_certificate_id,creation_time,org_structure_type,organization_id,"_need_update",update_time,phone_confirmed) VALUES
	 ('15BDC75D-533B-44BB-9539-3DDC8693F3D3'::uuid,'sbertransport1_test2_test@test-exchtest.sbrf.ru','Иван','Иавнов','+73123123124','Иванович','2016498','489A0090-1819-4C60-A611-572EA115C6A4'::uuid,
	 '3908803B-A08C-4D96-AA71-9DD5A6D3ABD4'::uuid,NULL,'15BDC75D-533B-44BB-9539-3DDC8693F3D3'::uuid,'ACTIVE'::corporate."active_status",'US-0008-00044612',NULL,NULL,NULL,NULL,true,'9900L01119','NONE'::corporate."itinerant",NULL,'2022-10-17 11:57:56.301','EXTERNAL'::corporate."structure_type",'621c288d-e348-46e5-a319-cbf61ef1e396'::uuid,false,'2025-11-10 16:15:16.791',true);