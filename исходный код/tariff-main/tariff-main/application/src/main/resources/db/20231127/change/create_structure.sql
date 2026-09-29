create table tariff_fleet.organization
(
    id            uuid not null
        constraint organization_pkey
            primary key,
    digit_id      numeric,
    active        boolean,
    official_name varchar(255)
);

comment
    on table tariff_fleet.organization is 'Организация';

comment
    on column tariff_fleet.organization.id is 'Идентификатор записи об организации';

comment
    on column tariff_fleet.organization.digit_id is 'Уникальный идентификатор (числовой)';

comment
    on column tariff_fleet.organization.active is 'Флаг активности';

comment
    on column tariff_fleet.organization.official_name is 'Служебное название';


create table tariff_fleet.department
(
    id                uuid         not null
        constraint department_pkey
            primary key,
    active            boolean,
    department_name   varchar(255) not null,
    human_readable_id varchar(255) not null,
    organization_id   uuid         not null
        constraint department_organization_id_fk
            references tariff_fleet.organization,
    parent_id         uuid
        constraint department_department_id_fk
            references tariff_fleet.department,
    easup_id          varchar(20)
);

comment
    on table tariff_fleet.department is 'Подразделение';

comment
    on column tariff_fleet.department.id is 'Идентификатор записи о подразделении';

comment
    on column tariff_fleet.department.active is 'Флаг активности';

comment
    on column tariff_fleet.department.department_name is 'Наименование подразделения';

comment
    on column tariff_fleet.department.human_readable_id is 'Человекочитаемый идентификатор';

comment
    on column tariff_fleet.department.organization_id is 'Идентификатор записи об организации';

comment
    on column tariff_fleet.department.parent_id is 'Идентификатор записи родителя в таблице department';

comment
    on column tariff_fleet.department.easup_id is 'Орг. единица';

create index department_parent_id_index
    on tariff_fleet.department (parent_id);

create table tariff_fleet.position
(
    id              uuid         not null
        constraint position_pkey
            primary key,
    active          boolean,
    organization_id uuid
        constraint position_organization_id_fk
            references tariff_fleet.organization,
    position_name   varchar(255) not null
);

comment
    on table tariff_fleet.position is 'Должность';

comment
    on column tariff_fleet.position.id is 'Идентификатор записи о должности';

comment
    on column tariff_fleet.position.active is 'Флаг активности';

comment
    on column tariff_fleet.position.organization_id is 'Идентификатор записи об организации';

comment
    on column tariff_fleet.position.position_name is 'Наименование позиции';


create table tariff_fleet.employee
(
    id                uuid         not null
        constraint employee_pkey
            primary key,
    active            boolean,
    human_readable_id varchar(255) not null,
    first_name        varchar(255) not null,
    last_name         varchar(255) not null,
    patronymic        varchar(255),
    mobile_phone      varchar(255),
    personnel_number  varchar(255) not null,
    user_id           uuid         not null
        constraint uk_user_employee
            unique,
    department_id     uuid         not null
        constraint employee_department_id_fk
            references tariff_fleet.department,
    position_id       uuid         not null
        constraint employee_position_id_fk
            references tariff_fleet.position,
    organization_id   uuid
        constraint employee_organization_id_fk
            references tariff_fleet.organization,
    cost_center       varchar(128)
);

comment
    on table tariff_fleet.employee is 'Сотрудник';

comment
    on column tariff_fleet.employee.id is 'Идентификатор записи о сотруднике';

comment
    on column tariff_fleet.employee.active is 'Флаг активности';

comment
    on column tariff_fleet.employee.human_readable_id is 'Человекочитаемый идентификатор';

comment
    on column tariff_fleet.employee.first_name is 'Имя';

comment
    on column tariff_fleet.employee.last_name is 'Фамилия';

comment
    on column tariff_fleet.employee.patronymic is 'Отчество';

comment
    on column tariff_fleet.employee.mobile_phone is 'Номер телефона';

comment
    on column tariff_fleet.employee.personnel_number is 'Табельный номер';

comment
    on column tariff_fleet.employee.user_id is 'Идентификатор записи с таблицы corporate.user';

comment
    on column tariff_fleet.employee.department_id is 'Идентификатор записи о подразделении';

comment
    on column tariff_fleet.employee.position_id is 'Идентификатор записи о должности';

comment
    on column tariff_fleet.employee.organization_id is 'Идентификатор записи об организации';

comment
    on column tariff_fleet.employee.cost_center is 'МВЗ';

create index employee_department_id_index
    on tariff_fleet.employee (department_id);

create index employee_organization_id_index
    on tariff_fleet.employee (organization_id);

-- create table tariff_fleet.urls
-- (
--     id      uuid not null
--         constraint users_urls_pkey
--             primary key,
--     url     text not null,
--     pattern text not null,
--     method  text not null
-- );
--
-- comment
--     on table tariff_fleet.urls is 'Ссылка';
--
-- comment
--     on column tariff_fleet.urls.id is 'Идентификатор записи ссылки';
--
-- comment
--     on column tariff_fleet.urls.url is 'Адрес ссылки';
--
-- comment
--     on column tariff_fleet.urls.pattern is 'Паттерн ссылки';
--
-- comment
--     on column tariff_fleet.urls.method is 'Наименование REST метода';
--
-- create table tariff_fleet.roles
-- (
--     role   text not null,
--     url_id uuid not null
--         constraint role_urls_fkey
--             references tariff_fleet.urls,
--     constraint user_roles_ukey
--         unique (url_id, role)
-- );
--
-- comment
--     on table tariff_fleet.roles is 'Связка роль-ссылка';
--
-- comment
--     on column tariff_fleet.roles.role is 'Наименование роли';
--
-- comment
--     on column tariff_fleet.roles.url_id is 'Идентификатор записи о ссылке';