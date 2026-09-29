ALTER TABLE corporate.employee
    ADD COLUMN IF NOT EXISTS
        changed_by varchar(100);

COMMENT ON COLUMN corporate.employee.changed_by is 'Идентификатор пользователя(учетной записи) внесшего изменения в запись';
