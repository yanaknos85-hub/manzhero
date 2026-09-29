alter table approvals.message_organization
    alter column digit_id set not null;

alter table approvals.message_position
    alter column self_approved set not null;

create unique index message_organization_digit_id_uindex
    on approvals.message_organization (digit_id);

alter table approvals.message_position
    add constraint message_position_message_organization_id_fk
        foreign key (organization_id) references approvals.message_organization;

alter table approvals.message_department
    alter column department_name set not null;

alter table approvals.message_department
    add constraint message_department_message_department_id_fk
        foreign key (parent_id) references approvals.message_department;

alter table approvals.message_department
    add constraint message_department_message_employee_id_fk
        foreign key (department_head_id) references approvals.message_employee;

alter table approvals.message_department
    add constraint message_department_message_organization_id_fk
        foreign key (organization_id) references approvals.message_organization;

alter table approvals.message_employee
    alter column first_name set not null;

alter table approvals.message_employee
    alter column last_name set not null;

alter table approvals.message_employee
    alter column personnel_number set not null;

alter table approvals.message_employee
    alter column department_id set not null;

alter table approvals.message_employee
    alter column position_id set not null;

create unique index message_employee_human_readable_id_uindex
    on approvals.message_employee (human_readable_id);

create unique index message_employee_user_id_uindex
    on approvals.message_employee (user_id);

alter table approvals.message_employee
    add constraint message_employee_message_department_id_fk
        foreign key (department_id) references approvals.message_department;

alter table approvals.message_employee
    add constraint message_employee_message_position_id_fk
        foreign key (position_id) references approvals.message_position;

alter table approvals.message_employee
    add constraint message_employee_message_employee_id_fk
        foreign key (supervisor_id) references approvals.message_employee;




