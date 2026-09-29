alter table if exists corporate.department
    add constraint UK_CODE unique (code);
alter table if exists corporate.department
    add constraint UK_STRUCTURE unique (structure_path);