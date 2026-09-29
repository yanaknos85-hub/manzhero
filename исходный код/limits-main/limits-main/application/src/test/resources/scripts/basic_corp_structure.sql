
INSERT INTO limits.organization (id, digit_id, active)
VALUES ('cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 8, true);
INSERT INTO limits.department (id, active, department_name, humanreadableid, organization_id, parent_id, code)
VALUES ('482e6dcb-03a9-4927-90b4-c7081114a9d8', true, 'ПАО «Сбербанк России» (ЦА)', 'DT-0008-00000161',
        'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', null, '11111');
INSERT INTO limits.employee (id, active, humanreadableid, first_name, last_name, patronymic,
                              personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('3cd35c19-fd39-413c-99a0-30f35bd642a8', true, 'US-0008-00044610', 'Петр', 'Петров', null,
        '2016497', '3cd35c19-fd39-413c-99a0-30f35bd642a8', '482e6dcb-03a9-4927-90b4-c7081114a9d8',
        '69a4d3f0-223b-46f5-8fed-0e856a1889dc', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6');
INSERT INTO limits.employee (id, active, humanreadableid, first_name, last_name, patronymic,
                              personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('7dd56ea0-fa38-400d-93a6-2a4ef4b7df70', true, 'US-0008-00044612', 'Александр', 'Александров', 'Александрович',
         '3016497', '7dd56ea0-fa38-400d-93a6-2a4ef4b7df70', '482e6dcb-03a9-4927-90b4-c7081114a9d8',
        '69a4d3f0-223b-46f5-8fed-0e856a1889dc', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6');

INSERT INTO limits.organization (id, digit_id, active)
VALUES ('621c288d-e348-46e5-a319-cbf61ef1e396', 11, true);
-- DEPARTMENT_2
INSERT INTO limits.department (id, active, department_name, humanreadableid, organization_id, parent_id, code)
VALUES ('489A0090-1819-4C60-A611-572EA115C6A4', true, 'Test2', 'DT-0008-00000222',
        '621c288d-e348-46e5-a319-cbf61ef1e396', null, '11112');
-- CHILD_DEPARTMENT_1
INSERT INTO limits.department (id, active, department_name, humanreadableid, organization_id, parent_id, code)
VALUES ('c3820eb5-e6a5-4fb6-a5c9-1e88a0e5055d', true, 'Test_Child_1', 'DT-0008-00000223',
        '621c288d-e348-46e5-a319-cbf61ef1e396', '489A0090-1819-4C60-A611-572EA115C6A4', '11113');
INSERT INTO limits.department (id, active, department_name, humanreadableid, organization_id, parent_id, code)
VALUES ('79fb1427-6e87-4755-bdde-fb3434d1bbd2', true, 'Test_Sub_Child_1', 'DT-0008-00000224',
        '621c288d-e348-46e5-a319-cbf61ef1e396', 'c3820eb5-e6a5-4fb6-a5c9-1e88a0e5055d', '11116');
INSERT INTO limits.employee (id, active, humanreadableid, first_name, last_name, patronymic,
                personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('15BDC75D-533B-44BB-9539-3DDC8693F3D3', true, 'US-0008-00044610', 'Иван', 'Иванов', 'Иванович',
        '2016498', '15BDC75D-533B-44BB-9539-3DDC8693F3D3', '489A0090-1819-4C60-A611-572EA115C6A4',
        '3908803B-A08C-4D96-AA71-9DD5A6D3ABD4', '621c288d-e348-46e5-a319-cbf61ef1e396');

INSERT INTO limits.organization (id, digit_id, active)
VALUES ('11501599-b498-4e9c-8b75-3e5899c445c0', 77, true);
-- DEPARTMENT_3
INSERT INTO limits.department (id, active, department_name, humanreadableid, organization_id, parent_id, code)
VALUES ('21f90644-fb63-4225-a794-c6062ad53e56', true, 'Test_dep_3', 'DT-0008-00000333',
        '11501599-b498-4e9c-8b75-3e5899c445c0', null, '11114');
-- CHILD_DEPARTMENT_3_2
INSERT INTO limits.department (id, active, department_name, humanreadableid, organization_id, parent_id, code)
VALUES ('e4cdf1e8-45d6-4ecf-8330-6b10760d256d', true, 'Test_dep_child_3_2', 'DT-0008-00003332',
        '11501599-b498-4e9c-8b75-3e5899c445c0', '21f90644-fb63-4225-a794-c6062ad53e56', '11115');
INSERT INTO limits.employee (id, active, humanreadableid, first_name, last_name, patronymic,
                              personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('c9e9192f-d2f3-4604-83f8-e863ce1bc192', true, 'US-0008-00033310', 'Иван', 'Иванов', 'Иванович',
        '2016138', 'c9e9192f-d2f3-4604-83f8-e863ce1bc192', '21f90644-fb63-4225-a794-c6062ad53e56',
        'd154bbb7-2221-450f-aa9a-deeb1576a477', '11501599-b498-4e9c-8b75-3e5899c445c0');