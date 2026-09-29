alter table approvals.message_organization
    add active boolean;

alter table approvals.message_department
    add active boolean;

alter table approvals.message_position
    add active boolean;

alter table approvals.message_employee
    add active boolean;

alter table approvals.message_position
    alter column self_approved set default false;

alter table approvals.message_employee
    rename column humanreadableid to human_readable_id;