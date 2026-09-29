--Удаление привязки заявок к группам исполнителей в request
UPDATE request.request_for_taxi SET executor_group_id=NULL, executor_group_name=NULL WHERE NOT executor_group_id IS NULL;
UPDATE request.request_for_carsharing SET executor_group_id=NULL, executor_group_name=NULL WHERE NOT executor_group_id IS NULL;
UPDATE request.request_for_group_transfer SET executor_group_id=NULL, executor_group_name=NULL WHERE NOT executor_group_id IS NULL;
UPDATE request.request_for_personal SET executor_group_id=NULL, executor_group_name=NULL WHERE NOT executor_group_id IS NULL;
UPDATE request.request_for_public SET executor_group_id=NULL, executor_group_name=NULL WHERE NOT executor_group_id IS NULL;
--Удаление привязки заявок к группам исполнителей в reports
UPDATE reports.request SET executor_group_id=NULL, executor_group_name=NULL WHERE NOT executor_group_id IS NULL;
--Удаление привязки заявок к группам исполнителей в metrics
UPDATE metrics.request SET executor_group_id=NULL WHERE NOT executor_group_id IS NULL;
--Удаление значений справочников групп исполнителей
truncate corporate.executor_group_customer;
truncate corporate.executor_group_department;
truncate corporate.executor_group_executor;
truncate corporate.executor_group_geo_zone;
truncate corporate.executor_group_organization;
truncate corporate.executor_group;




