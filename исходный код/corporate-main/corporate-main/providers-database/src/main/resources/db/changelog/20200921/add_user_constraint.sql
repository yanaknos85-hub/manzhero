alter table if exists corporate.employee
    add constraint UK_EMPLOYEE_USER unique (user_id);