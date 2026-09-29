-- Убираем ограничение внешнего ключа на employee_id для тестов
ALTER TABLE corporate.employee_document DROP CONSTRAINT IF EXISTS employee_document_employee_id_fk;
-- Убираем ограничение внешнего ключа на car_id для тестов
ALTER TABLE corporate.employee_document DROP CONSTRAINT IF EXISTS employee_document_car_id_fk;
