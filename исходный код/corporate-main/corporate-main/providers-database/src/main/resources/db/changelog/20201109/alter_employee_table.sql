alter table corporate.employee
    add column if not exists room varchar(255);
alter table corporate.employee
    add column if not exists consent boolean;