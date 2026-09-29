-- DEPARTMENT 1
INSERT INTO tariff_fleet.department
(id, active, department_name, human_readable_id, organization_id, parent_id, easup_id)
VALUES('000098ba-5c12-423f-ba6a-de9c76db31a5', true, 'Service Support Group', 'DT-0047-00000441', '46a3b980-d801-499b-87eb-be0bf3f2de53', NULL, '10243018')
ON CONFLICT DO NOTHING;

-- DEPARTMENT 2
INSERT INTO tariff_fleet.department
(id, active, department_name, human_readable_id, organization_id, parent_id, easup_id)
VALUES('000091e3-48cf-4678-beba-43b6dd695e67', true, 'Partner Relations Department', 'DT-0045-00000117','46a3b980-d801-499b-87eb-be0bf3f2de53', '000098ba-5c12-423f-ba6a-de9c76db31a5', '10181778')
ON CONFLICT DO NOTHING;

-- DEPARTMENT 3
INSERT INTO tariff_fleet.department
(id, active, department_name, human_readable_id, organization_id, parent_id, easup_id)
VALUES('0003311f-3ce9-4d54-b9f0-5537ff7c5689', false, 'Дополнительный офис № 8606/0107', 'DT-0479-00000001', 'c93b30b8-4a35-4c29-8d92-2fb3279a3841', NULL, NULL)
ON CONFLICT DO NOTHING;