INSERT INTO corporate."attribute" (id,"name","status") VALUES
	 ('8ce404c1-5aed-43d0-abfa-d00443dde7ef'::uuid,'Тест','ACTIVE'::corporate."active_status");

INSERT INTO corporate.employee_attribute (employee_id,attribute_id) VALUES
	 ('3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'8ce404c1-5aed-43d0-abfa-d00443dde7ef'::uuid);

INSERT INTO corporate.trip_purpose (id,"label",active,organization,purpose_parent_label,purpose_type,icon) VALUES
	 ('487a8825-bae3-4e2a-aec0-c900bb6de5ca'::uuid,'Доставка сотрудников в ночное время',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'8515743d-2221-44c9-9b12-f5e85c1c4aae','CORPORATE',NULL),
	 ('508808cb-5643-462c-aede-fb44c2a3b92f'::uuid,'начало/окончание работы по графику в ночное время',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'ebe45f3d-2769-4220-bbf1-95d467012859','CORPORATE',NULL),
	 ('dde61370-bfc4-4b1f-8f19-983bb63d6754'::uuid,'1-По дням недели отправления (Шаргаев)',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'f87b0ede-31cf-4409-be59-8549a8054d97','CORPORATE',NULL),
	 ('7feff188-80cc-41a1-a7ae-03070d47cbf8'::uuid,'2-По дням недели отправления (Шаргаев)',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'b0b221fc-6e6f-4944-92ea-e799770c683d','CORPORATE',NULL),
	 ('d5ff383b-8798-4e5e-983f-78a674448cad'::uuid,'Доп. настройки (Шаргаев)',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'616e9d0e-b056-48a2-a9b1-259344276694','CORPORATE',NULL),
	 ('be5aa96b-eaed-4a0b-9920-bf28e388f93f'::uuid,'Доставка банковских карт (для деп_02)',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'0aebf204-17d3-4285-bc4d-f642ca97c7b1','CORPORATE',NULL),
	 ('d89a3c89-b321-48c4-b5c2-6e415a849f60'::uuid,'Тестовая-цель',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'90a83c3a-0fd5-40a1-b189-07a6d3d0d961','CORPORATE',NULL),
	 ('c883d839-2b26-42f9-9f2d-562869ebe85b'::uuid,'2-По дате отправления (Шаргаев)',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'128b53d1-92fd-4f8d-883a-4d7d52772569','CORPORATE',NULL),
	 ('d6c62dc1-10ad-4b29-bef0-2a9def53626c'::uuid,'2-По времени отправления (Шаргаев)',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'f3f7f500-48c9-445f-9ec2-20ff49735f4c','CORPORATE',NULL),
	 ('c534ff31-6e1f-4e12-9405-86ffda7ecf3a'::uuid,'Тест-регресс',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'c4ba6e9c-d0fe-46eb-93ea-99139caa731a','CORPORATE',NULL),
	 ('b0de6628-3ab8-4f0b-9ad1-c1e1b5aabb69'::uuid,'1-По времени отправления (Шаргаев)',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'d882d1b0-3605-434a-aef1-79a362a2984d','CORPORATE',NULL),
	 ('e73bb382-96a3-4f17-a45d-92d9a3c5ad85'::uuid,'Доставка банковских карт',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'7abeab81-b4d1-4e1c-ad5b-ba5879cdebfa','CORPORATE',NULL),
	 ('8d26e07f-1522-4359-a0dc-906575ce9a67'::uuid,'1-По дате отправления (Шаргаев)',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'4b573601-0306-4ac6-8c41-2cbdd8a723db','CORPORATE',NULL),
	 ('32bb3a40-57d7-4992-be3a-f4aac2360779'::uuid,'Встреча с клиентами снова',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_448','CORPORATE',NULL),
	 ('68fab835-68d7-4088-9ce0-cbfadc0767b1'::uuid,'Проведение проверки залогов',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_627','CORPORATE',NULL),
	 ('30440113-971a-410f-86bc-6d8f44512537'::uuid,'подразделение Fortest',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_291','CORPORATE',NULL),
	 ('91bf3e90-0e53-4ea4-b53e-57c68f77c615'::uuid,'Тестовый_департамент_01',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_897','CORPORATE',NULL),
	 ('141099fe-8415-4e0c-817f-ba4f27558a7c'::uuid,'Корпоративная поездка',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_114','CORPORATE',NULL),
	 ('1a35bc75-4d83-472d-a02f-424057894c9a'::uuid,'Доставка работников в ночное время 19-03',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_144','CORPORATE',NULL),
	 ('be035ae1-ab84-4092-bde6-08ed35d485c3'::uuid,'Гемба',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_809','CORPORATE',NULL),
	 ('c009154a-7f84-4b1f-bcdb-8d27586ec0ce'::uuid,'Встреча с клиентами снова',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_1302','CORPORATE',NULL),
	 ('251d66f6-45fc-4e21-809f-cff002fc2b43'::uuid,'Проведение наставничества ВСП',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_217','CORPORATE',NULL),
	 ('3cb2f5bc-9b17-4672-86d1-3745c76a8eb7'::uuid,'Проведение контрольных процедурс',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_18','CORPORATE',NULL),
	 ('274252ef-ab86-402b-990c-f12baea7928b'::uuid,'Встречи с контрагентами',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_230','CORPORATE','PURPOSE_M_CONTR'),
	 ('54e4950a-d96c-45b2-8ee3-306ec953712d'::uuid,'Цель Орг структура 1',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_497','CORPORATE',NULL),
	 ('8ec44633-74b3-4304-9e70-6528e607d07d'::uuid,'Встреча с клиентами снова',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_1302','CORPORATE',NULL),
	 ('5e0e56f4-b3a3-45c1-a30e-d6213bce6238'::uuid,'Выезды на аварии',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_554','CORPORATE',NULL),
	 ('5f87195b-e4a9-4111-93da-e8c6e797594b'::uuid,'Производственные (банковские) мероприятия',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_570','CORPORATE','PURPOSE_A_PROD'),
	 ('66c1e9cf-6e81-4c8e-977e-887c1ce75a25'::uuid,'Выезд на конференцию',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_608','CORPORATE',NULL),
	 ('8b8f1445-1e97-46e2-8977-a28aa1435a97'::uuid,'Поездка в сервис-поинт',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_853','CORPORATE',NULL),
	 ('c12209e9-4b7b-423d-aae0-29eab6a33c07'::uuid,'Доставка подменного фонда работников в ВСП и обратно',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_1206','CORPORATE','PURPOSE_D_VSP'),
	 ('ce4503f3-aa09-4ba3-9b75-289c93261321'::uuid,'Контроль СМР на объектах переформатирования',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_1289','CORPORATE',NULL),
	 ('dac2ed14-27f1-11eb-9a28-305a3a7d9fe6'::uuid,'Выезды в государственные органы',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_1378','CORPORATE','PURPOSE_V_GOV'),
	 ('dac2ed21-27f1-11eb-9a28-305a3a7d9fe6'::uuid,'Перемещение ВСП',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_1385','CORPORATE','PURPOSE_MV_VSP'),
	 ('dac2ed27-27f1-11eb-9a28-305a3a7d9fe6'::uuid,'Выезды для участия в коллегиальных органах/рабочих группах',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_1391','CORPORATE','PURPOSE_V_GROUPS'),
	 ('e008a30d-6c61-4119-9fda-17861544a869'::uuid,'Доставка сотрудников  в ночное время 22-06',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_1421','PERSONAL',NULL),
	 ('f786a44e-822f-45b8-b83f-51b34dbd8edc'::uuid,'Встречи с клиентами',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid,'parent_label_1566','CORPORATE',NULL);


INSERT INTO corporate.trip_purpose_statistic (id,employee,trip_purpose,usage_count) VALUES
	 ('ffae3c9d-5e7c-4b15-89ed-17753638a057'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'5f87195b-e4a9-4111-93da-e8c6e797594b'::uuid,27),
	 ('3b6b7f78-9f53-4c19-ac55-b2a26314117e'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'66c1e9cf-6e81-4c8e-977e-887c1ce75a25'::uuid,13),
	 ('f65b40a6-d229-4f08-85bb-e2dba5ff0d58'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'251d66f6-45fc-4e21-809f-cff002fc2b43'::uuid,17),
	 ('98bf7153-7aae-4bde-be9b-34a78e17435a'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'dac2ed27-27f1-11eb-9a28-305a3a7d9fe6'::uuid,78),
	 ('98901f29-d7fa-45ea-85b8-29fdb77c49e0'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'dac2ed21-27f1-11eb-9a28-305a3a7d9fe6'::uuid,30),
	 ('165cf8ff-d361-4fd7-b8f3-3d990c15970f'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'274252ef-ab86-402b-990c-f12baea7928b'::uuid,13058),
	 ('dfab8e92-bb12-462b-9aa8-dc909d296a01'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'c12209e9-4b7b-423d-aae0-29eab6a33c07'::uuid,45),
	 ('0c8069e1-6962-4625-8b86-3dffb1fa2b17'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'51373366-e2ce-40a2-897b-a1b2236f14f0'::uuid,6),
	 ('3793db74-19de-4300-9ab2-41534f8731b0'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'1130d5bb-571b-4f71-bd52-78e399cfd5bf'::uuid,6),
	 ('f487e894-f7e0-486d-affb-747f038f1fac'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'e1038e1f-a76e-4c9a-8456-a52dde2f3c82'::uuid,6),
	 ('b8d9ed68-9e77-4741-a7a7-0a2a34d984e5'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'7455d356-6414-4b64-b6c3-8e6897c805e0'::uuid,6),
	 ('cd34c7c3-a5d0-42d0-bcc2-037cbb7a2529'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'4bc99658-3ee1-46f3-8e2e-c1038e0a1e5c'::uuid,4),
	 ('db4aa660-4954-47e7-8ecc-c61931c6ba41'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'ddd3f82b-c6e0-4086-8c8a-bfb0991ad9a3'::uuid,7),
	 ('84259599-2012-4f66-9c70-49519d08526b'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'f786a44e-822f-45b8-b83f-51b34dbd8edc'::uuid,19),
	 ('8cef2db8-28fb-464b-9b3f-fd0b4529e1a6'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'f3f5c243-a6a6-436f-854d-5389288c9388'::uuid,2),
	 ('0072d6c6-f88f-4f14-af10-af801cb7f68e'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'141099fe-8415-4e0c-817f-ba4f27558a7c'::uuid,4),
	 ('40f925b0-462c-4675-8ef4-4dc887f48071'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'814bad3b-7e9f-4a5f-8465-268817b75cb4'::uuid,4),
	 ('138442c7-620a-41f0-8b4e-1a7f4e47d56c'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'d5ff383b-8798-4e5e-983f-78a674448cad'::uuid,14),
	 ('5f7d8ae1-f3cf-4f02-b6ba-d6672c50a580'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'1a35bc75-4d83-472d-a02f-424057894c9a'::uuid,142),
	 ('c6fcf487-5a74-4ef0-bf52-650f64dfe4cd'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'dc70dfed-0a8c-4a03-a0f5-2a83ce709ae2'::uuid,1),
	 ('15b3a01d-34f1-4c13-807a-6f02baba3e39'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'dde61370-bfc4-4b1f-8f19-983bb63d6754'::uuid,13),
	 ('6ac84e94-e4dc-4fd8-a3f2-44d96b01da5e'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'dac2ed14-27f1-11eb-9a28-305a3a7d9fe6'::uuid,94),
	 ('f59bd38c-1b66-4530-a315-ac57fdccca8a'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'fad59dcd-3e14-40f0-a343-cb5857b09885'::uuid,2),
	 ('24f4c82f-1cba-49bb-91b8-196d919a5883'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'7feff188-80cc-41a1-a7ae-03070d47cbf8'::uuid,8),
	 ('4f98d0fe-9246-413a-9363-df08072da0ca'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'4eac82d1-8b32-4bc4-b1c1-88947cea4604'::uuid,4),
	 ('a981f9e7-efe1-4c4c-8322-c90782084abf'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'99780a6f-1d21-4e5f-aaab-0811d35a52e8'::uuid,4),
	 ('822bd930-9c1a-4be2-a8d6-391b94d0165f'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'20706538-a9db-4258-9b23-0d7813434157'::uuid,1),
	 ('08e32a95-d377-4f1a-bb5e-ae95f46c9a83'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'d6c62dc1-10ad-4b29-bef0-2a9def53626c'::uuid,6),
	 ('6c5c464a-569e-466d-88db-524318424945'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'633fa1ec-7a60-4c28-90fc-bf78475c0a22'::uuid,4);

INSERT INTO corporate.trip_purpose_department (id,trip_purpose,department) VALUES
	 ('9754ce28-861c-43f1-9521-290667727bb2'::uuid,'dac2ed27-27f1-11eb-9a28-305a3a7d9fe6'::uuid,'489A0090-1819-4C60-A611-572EA115C6A4'::uuid),
	 ('955d14fd-48e6-4a7a-bc46-232adfe75aae'::uuid,'91bf3e90-0e53-4ea4-b53e-57c68f77c615'::uuid,'489A0090-1819-4C60-A611-572EA115C6A4'::uuid),
	 ('94cc3a75-18b2-4eeb-b344-c763550c0443'::uuid,'54e4950a-d96c-45b2-8ee3-306ec953712d'::uuid,'489A0090-1819-4C60-A611-572EA115C6A4'::uuid),
	 ('fefc917e-c8c9-4536-8dac-88dc83777371'::uuid,'c534ff31-6e1f-4e12-9405-86ffda7ecf3a'::uuid,'482e6dcb-03a9-4927-90b4-c7081114a9d8'::uuid),
	 ('e8a6b263-f102-4c56-b70d-a6b13aa092f7'::uuid,'508808cb-5643-462c-aede-fb44c2a3b92f'::uuid,'482e6dcb-03a9-4927-90b4-c7081114a9d8'::uuid),
	 ('ec0d8e61-e969-423d-938b-a093573c7a71'::uuid,'e73bb382-96a3-4f17-a45d-92d9a3c5ad85'::uuid,'482e6dcb-03a9-4927-90b4-c7081114a9d8'::uuid),
	 ('553d0ca8-1b46-45c5-8b4f-18f96b0b5634'::uuid,'be5aa96b-eaed-4a0b-9920-bf28e388f93f'::uuid,'482e6dcb-03a9-4927-90b4-c7081114a9d8'::uuid);

INSERT INTO corporate.trip_purpose_date (id,trip_purpose,purpose_date,purpose_date_end) VALUES
	 ('98bd8513-3a8a-456c-b6bb-7006a8516b4a'::uuid,'c12209e9-4b7b-423d-aae0-29eab6a33c07'::uuid,'2025-10-09 10:22:24.232','2025-10-10 10:22:24.232'),
	 ('a1aa0b97-a3b6-454f-8d65-3c36af9879dd'::uuid,'d89a3c89-b321-48c4-b5c2-6e415a849f60'::uuid,'2025-10-27 06:55:38.915','2025-10-28 06:55:38.915'),
	 ('866a2ef2-1253-4c2d-a1cc-350c25940c8d'::uuid,'c883d839-2b26-42f9-9f2d-562869ebe85b'::uuid,'2025-11-17 11:30:54.265','2025-11-23 11:30:54.265'),
	 ('89e7a880-f545-4ff4-abcb-79afdd9e0338'::uuid,'8d26e07f-1522-4359-a0dc-906575ce9a67'::uuid,'2025-11-10 11:31:23.269','2025-11-16 11:31:23.269');

INSERT INTO corporate.trip_purpose_time (id,trip_purpose,start_time,end_time) VALUES
	 ('77f23700-a1b0-4daf-9519-a7f0fe335998'::uuid,'1a35bc75-4d83-472d-a02f-424057894c9a'::uuid,'2022-11-17 19:00:00.000','2022-11-17 03:00:00.000'),
	 ('f36ad12c-ba69-4b00-bbbd-c327a48c315b'::uuid,'e008a30d-6c61-4119-9fda-17861544a869'::uuid,'2023-06-22 22:00:00.000','2023-06-22 06:00:00.000'),
	 ('78bb5110-acac-47d8-9137-7f2acd6ee6bf'::uuid,'141099fe-8415-4e0c-817f-ba4f27558a7c'::uuid,'2023-11-03 13:00:00.000','2023-11-03 15:00:00.000'),
	 ('8a80c8fe-a98f-4351-b503-eda294076f7f'::uuid,'d6c62dc1-10ad-4b29-bef0-2a9def53626c'::uuid,'2025-04-30 13:00:00.000','2025-04-30 18:59:00.000'),
	 ('f214e845-cda0-4036-9711-192ba1845b7d'::uuid,'b0de6628-3ab8-4f0b-9ad1-c1e1b5aabb69'::uuid,'2025-04-30 08:00:00.000','2025-04-30 12:59:00.000'),
	 ('96c55292-9ba7-4c7d-a876-455965deb696'::uuid,'487a8825-bae3-4e2a-aec0-c900bb6de5ca'::uuid,'2025-10-16 22:00:00.000','2025-10-16 06:00:00.000'),
	 ('e0ba8e2b-8e94-4265-8531-29f88579a2f4'::uuid,'508808cb-5643-462c-aede-fb44c2a3b92f'::uuid,'2025-10-16 22:00:00.000','2025-10-16 06:00:00.000'),
	 ('94b92301-9373-4808-a58b-9a4fabce43d5'::uuid,'be5aa96b-eaed-4a0b-9920-bf28e388f93f'::uuid,'2025-10-16 07:00:00.000','2025-10-16 20:00:00.000'),
	 ('206b37ad-c2e7-4a3b-b617-ce4fcb7d2154'::uuid,'d89a3c89-b321-48c4-b5c2-6e415a849f60'::uuid,'2025-10-27 08:00:00.000','2025-10-27 12:00:00.000'),
	 ('b565719c-def7-4179-86b9-98320138421f'::uuid,'e73bb382-96a3-4f17-a45d-92d9a3c5ad85'::uuid,'2025-10-16 00:00:00.000','2025-10-16 20:00:00.000');


INSERT INTO corporate.trip_purpose_weekday (id,trip_purpose,weekday) VALUES
	 ('1b088430-fe8e-4e08-8511-3a5dbdc27fa9'::uuid,'c12209e9-4b7b-423d-aae0-29eab6a33c07'::uuid,'TUESDAY'),
	 ('b39b74a0-6398-486b-8da2-374f31634233'::uuid,'c12209e9-4b7b-423d-aae0-29eab6a33c07'::uuid,'WEDNESDAY'),
	 ('e7620ef2-1b9b-4072-a045-8a650ab19a09'::uuid,'c12209e9-4b7b-423d-aae0-29eab6a33c07'::uuid,'THURSDAY'),
	 ('c984d2c1-0deb-47e1-a5f0-5f43b2a0a53a'::uuid,'dde61370-bfc4-4b1f-8f19-983bb63d6754'::uuid,'MONDAY'),
	 ('d887dc48-a9cb-4963-b48f-09df813625b0'::uuid,'dde61370-bfc4-4b1f-8f19-983bb63d6754'::uuid,'TUESDAY'),
	 ('7b5b51f2-fc43-492a-bcf5-4538b98fbe60'::uuid,'dde61370-bfc4-4b1f-8f19-983bb63d6754'::uuid,'WEDNESDAY'),
	 ('d0de4b1a-a4f3-40ab-8e7c-970e6d157d02'::uuid,'7feff188-80cc-41a1-a7ae-03070d47cbf8'::uuid,'THURSDAY'),
	 ('620ffa82-dbe9-4087-924b-5428b8bbaff2'::uuid,'7feff188-80cc-41a1-a7ae-03070d47cbf8'::uuid,'FRIDAY'),
	 ('7391b69e-1e6c-4101-b4eb-e4a1fcf03b11'::uuid,'7feff188-80cc-41a1-a7ae-03070d47cbf8'::uuid,'SATURDAY'),
	 ('2c43157f-c7cf-45c2-bcb1-6f3cef2206be'::uuid,'7feff188-80cc-41a1-a7ae-03070d47cbf8'::uuid,'SUNDAY'),
	 ('3b732489-6c0d-499f-be81-ae801d60f899'::uuid,'be035ae1-ab84-4092-bde6-08ed35d485c3'::uuid,'FRIDAY'),
	 ('609f1646-048a-4449-8ed6-c4535b2a6ea7'::uuid,'c534ff31-6e1f-4e12-9405-86ffda7ecf3a'::uuid,'MONDAY'),
	 ('6a183f22-3073-45fc-86c2-8dbfd61f525b'::uuid,'c534ff31-6e1f-4e12-9405-86ffda7ecf3a'::uuid,'TUESDAY'),
	 ('479dce7a-afd7-43c0-8971-8cfca268e937'::uuid,'c534ff31-6e1f-4e12-9405-86ffda7ecf3a'::uuid,'WEDNESDAY');

