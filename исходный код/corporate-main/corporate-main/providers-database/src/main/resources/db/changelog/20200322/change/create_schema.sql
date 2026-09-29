CREATE SCHEMA corporate;

create table corporate.department
(
    id              uuid         not null,
    code            varchar(255) not null,
    department_name varchar(255) not null,
    structure_path  varchar(255) not null,
    location        varchar(255),
    department_head uuid,
    organization_id uuid         not null,
    parent_id       uuid,
    primary key (id)
);

create table corporate.employee
(
    id               uuid         not null,
    email            varchar(255),
    first_name       varchar(255) not null,
    last_name        varchar(255) not null,
    mobile_phone     varchar(255),
    patronymic       varchar(255),
    personnel_number varchar(255),
    delegated_by     uuid,
    department_id    uuid         not null,
    position_id      uuid         not null,
    supervisor_id    uuid,
    primary key (id)
);

create table corporate.organization
(
    id            uuid         not null,
    address       varchar(255) not null,
    official_name varchar(255) not null,
    primary key (id)
);

create table corporate.personal_auto
(
    id                uuid         not null,
    brand_name        varchar(255),
    engine_volume     int4,
    insurance_number  varchar(255),
    model             varchar(255),
    owner_info        varchar(255),
    reg_cert          varchar(255),
    reg_number        varchar(255) not null,
    corporate_user_id uuid         not null,
    primary key (id)
);

create table corporate.position
(
    id              uuid         not null,
    position_name   varchar(255) not null,
    self_approved   boolean,
    organization_id uuid         not null,
    primary key (id)
);

alter table if exists corporate.department
    add constraint UK_CODE unique (code);
alter table if exists corporate.department
    add constraint UK_STRUCTURE unique (structure_path);
alter table if exists corporate.employee
    add constraint UK_EMAIL unique (email);
alter table if exists corporate.employee
    add constraint UK_MOBILE unique (mobile_phone);
alter table if exists corporate.employee
    add constraint UK_PERSONNEL_NUMBER unique (personnel_number);
alter table if exists corporate.organization
    add constraint UK_OFFICIAL unique (official_name);
alter table if exists corporate.personal_auto
    add constraint UK_REG_CERT unique (reg_cert);
alter table if exists corporate.personal_auto
    add constraint UK_REG_NUM unique (reg_number);
create table corporate.employee_transport_types
(
    employee_id    uuid not null,
    transport_type varchar(255)
);
create table corporate.position_taxi_classes
(
    position_id uuid not null,
    taxi_class  varchar(255)
);
alter table if exists corporate.department
    add constraint FK_DEPARTMENT_HEAD foreign key (department_head) references corporate.employee;
alter table if exists corporate.department
    add constraint FK_ORGANIZATION_ID foreign key (organization_id) references corporate.organization;
alter table if exists corporate.department
    add constraint FK_DEPARTMENT_PARENT foreign key (parent_id) references corporate.department;
alter table if exists corporate.employee
    add constraint FK_EMPLOYEE_DELEGATED foreign key (delegated_by) references corporate.employee;
alter table if exists corporate.employee
    add constraint FK_EMPLOYEE_DEPARTMENT foreign key (department_id) references corporate.department;
alter table if exists corporate.employee
    add constraint FK_EMPLOYEE_POSITION foreign key (position_id) references corporate.position;
alter table if exists corporate.employee
    add constraint FK_EMPLOYEE_SUPERVISOR foreign key (supervisor_id) references corporate.employee;
alter table if exists corporate.personal_auto
    add constraint FK_AUTO_USER foreign key (corporate_user_id) references corporate.employee;
alter table if exists corporate.position
    add constraint FK_POSITION_ORGANIZATION foreign key (organization_id) references corporate.organization;
alter table if exists corporate.employee_transport_types
    add constraint FK_TRANSORT_TYPE_EMPLOYEE foreign key (employee_id) references corporate.employee;
alter table if exists corporate.position_taxi_classes
    add constraint FK_TAXI_CLASS_EMPLOYEE foreign key (position_id) references corporate.position;
;;