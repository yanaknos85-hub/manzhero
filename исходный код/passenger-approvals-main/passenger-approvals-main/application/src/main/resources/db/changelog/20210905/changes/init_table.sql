create schema approvals;
create table approvals.trip_request_approvals
(
    id                       uuid                     not null
        constraint approvals_pkey
            primary key,
    author_id                uuid                     not null,
    actor_id                 uuid                     not null,
    action_id                uuid                     not null
        constraint approval_action_uk
            unique,
    status                   text default 'NEW'::text not null,
    creation_time            timestamp                not null,
    end_time                 timestamp,
    deadline                 timestamp,
    approved_by_id           uuid,
    transport_type           text,
    trip_class               text,
    desired_date             timestamp,
    trip_purpose_id          uuid,
    expected_cost            double precision,
    expected_time            bigint,
    expected_distance        double precision,
    human_readable_id        varchar(255),
    passenger_count          integer,
    waypoints                jsonb,
    reason                   text,
    public_compensation_type text,
    shared_ride_id           bigint,
    shared_ride_owner        boolean,
    tariff_id                uuid
);

comment on table approvals.trip_request_approvals is 'Таблица согласований';

comment on column approvals.trip_request_approvals.id is 'Идентификатор согласований';

comment on column approvals.trip_request_approvals.author_id is 'Идентификатор автора';

comment on column approvals.trip_request_approvals.actor_id is 'Идентификатор участника';

comment on column approvals.trip_request_approvals.action_id is 'Идентификатор сущности согласования';

comment on column approvals.trip_request_approvals.status is 'Статус согласования (NEW, EDITED, ACCEPTED, DECLINED, CANCELLED)';

comment on column approvals.trip_request_approvals.creation_time is 'Время создания';

comment on column approvals.trip_request_approvals.end_time is 'Время окончания события';

comment on column approvals.trip_request_approvals.deadline is 'Контрольный срок';

comment on column approvals.trip_request_approvals.approved_by_id is 'Согласующий';

comment on column approvals.trip_request_approvals.public_compensation_type is 'Тип компенсации';

comment on column approvals.trip_request_approvals.shared_ride_id is 'Идентификатор совместной поездки';

comment on column approvals.trip_request_approvals.shared_ride_owner is 'Признак владельца личного транспорта';

comment on column approvals.trip_request_approvals.tariff_id is 'ID тарифа';



create index approval_actor_id_idx
    on approvals.trip_request_approvals (actor_id);

create index approval_action_id_idx
    on approvals.trip_request_approvals (action_id);

create index approval_desired_date_idx
    on approvals.trip_request_approvals (desired_date);

create index tr_shared_ride_id_idx
    on approvals.trip_request_approvals (shared_ride_id);

create table approvals.message_organization
(
    id       uuid not null
        constraint organization_pkey
            primary key,
    digit_id numeric
);

comment on table approvals.message_organization is 'Организации';

comment on column approvals.message_organization.id is 'Уникальный идентификатор';

comment on column approvals.message_organization.digit_id is 'Числовой ID';



create table approvals.message_department
(
    id                 uuid not null
        constraint department_pkey
            primary key,
    organization_id    uuid not null,
    parent_id          uuid,
    department_name    varchar(255),
    department_head_id uuid
);

comment on table approvals.message_department is 'Отделы';

comment on column approvals.message_department.id is 'Уникальный идентификатор';

comment on column approvals.message_department.organization_id is 'Идентификатор организации (внешний ключ)';

comment on column approvals.message_department.parent_id is 'Идентификатор отдела (внешний ключ)';

comment on column approvals.message_department.department_name is 'Название отдела';

comment on column approvals.message_department.department_head_id is 'Идентификатор руководителя';



create table approvals.message_position
(
    id              uuid not null
        constraint position_pkey
            primary key,
    self_approved   boolean,
    organization_id uuid not null
);

comment on table approvals.message_position is 'Должности';

comment on column approvals.message_position.id is 'Уникальный идентификатор';

comment on column approvals.message_position.self_approved is 'Самосогласование';

comment on column approvals.message_position.organization_id is 'Идентификатор организации (внешний ключ)';



create table approvals.message_employee
(
    id               uuid not null
        constraint employee_pkey
            primary key,
    patronymic       varchar(255),
    personnel_number varchar(255),
    user_id          uuid,
    delegated_by     uuid,
    department_id    uuid,
    position_id      uuid,
    supervisor_id    uuid,
    humanreadableid  varchar(100),
    first_name       varchar(255),
    last_name        varchar(255)
);

comment on table approvals.message_employee is 'Сотрудники';

comment on column approvals.message_employee.id is 'Уникальный идентификатор';

comment on column approvals.message_employee.patronymic is 'отчество';

comment on column approvals.message_employee.personnel_number is 'Табельный номер';

comment on column approvals.message_employee.user_id is 'Идентификатор пользователя (внешний ключ)';

comment on column approvals.message_employee.delegated_by is 'Делегат';

comment on column approvals.message_employee.department_id is 'Идентификатор отдела (внешний ключ)';

comment on column approvals.message_employee.position_id is 'Идентификатор должности (внешний ключ)';

comment on column approvals.message_employee.supervisor_id is 'Идентификатор руководителя (внешний ключ)';

comment on column approvals.message_employee.humanreadableid is 'Уникальный идентификатор (человекочитаемый ID)';

comment on column approvals.message_employee.first_name is 'Имя';

comment on column approvals.message_employee.last_name is 'Фамилия';



create table approvals.limit_reservation_responses
(
    id              uuid                     not null
        constraint limit_reservation_responses_pkey
            primary key,
    trip_request_id uuid                     not null
        constraint limit_reservation_responses_trip_request_id_key
            unique,
    status          text default 'NEW'::text not null
);

comment on table approvals.limit_reservation_responses is 'Таблица хранения сообщений об ответах резервирования лимита заявки';

comment on column approvals.limit_reservation_responses.trip_request_id is 'идентификатор заявки, для которой был осуществлен зпрос на резервирование';

comment on column approvals.limit_reservation_responses.status is 'Статус ответа резервирования лимита заявки';



create table approvals.corp_delegate
(
    id             uuid not null
        constraint corp_delegate_pkey
            primary key,
    start_date     date,
    end_date       date,
    delegate_id    uuid,
    supervisor_id  uuid,
    transport_type text
);



create table approvals."limit"
(
    id         uuid              not null
        constraint limit_pkey
            primary key,
    year       integer           not null,
    sum        integer           not null,
    reserve    integer default 0 not null,
    limit_type varchar(20)       not null,
    parent_id  uuid,
    owner_id   uuid              not null,
    deleted    boolean default false,
    period     numeric
);

create table approvals.final_trip_approvals
(
    id                       uuid                     not null
        constraint final_trip_approvals_pkey
            primary key,
    author_id                uuid                     not null,
    actor_id                 uuid                     not null,
    action_id                uuid                     not null
        constraint ft_approval_action_uk
            unique,
    status                   text default 'NEW'::text not null,
    creation_time            timestamp                not null,
    end_time                 timestamp,
    deadline                 timestamp,
    approved_by_id           uuid,
    transport_type           text                     not null,
    trip_class               text,
    desired_date             timestamp                not null,
    trip_purpose_id          uuid                     not null,
    expected_cost            double precision,
    expected_time            bigint,
    expected_distance        double precision,
    human_readable_id        varchar(255),
    passenger_count          integer,
    reason                   text,
    waypoints                jsonb,
    public_compensation_type text,
    tariff_id                uuid
);

comment on table approvals.final_trip_approvals is 'Таблица согласований';

comment on column approvals.final_trip_approvals.id is 'Идентификатор согласований';

comment on column approvals.final_trip_approvals.author_id is 'Идентификатор автора';

comment on column approvals.final_trip_approvals.actor_id is 'Идентификатор участника';

comment on column approvals.final_trip_approvals.action_id is 'Идентификатор сущности согласования';

comment on column approvals.final_trip_approvals.status is 'Статус согласования (NEW, EDITED, ACCEPTED, DECLINED, CANCELLED)';

comment on column approvals.final_trip_approvals.creation_time is 'Время создания';

comment on column approvals.final_trip_approvals.deadline is 'Контрольный срок';

comment on column approvals.final_trip_approvals.approved_by_id is 'Согласующий';

comment on column approvals.final_trip_approvals.transport_type is 'Тип транспорта';

comment on column approvals.final_trip_approvals.trip_class is 'Класс такси';

comment on column approvals.final_trip_approvals.desired_date is 'Дата поездки';

comment on column approvals.final_trip_approvals.trip_purpose_id is 'Цель поездки';

comment on column approvals.final_trip_approvals.expected_cost is 'Ожидаемая стоиомсть поезки';

comment on column approvals.final_trip_approvals.expected_time is 'Ожидаемая длительность поезки';

comment on column approvals.final_trip_approvals.expected_distance is 'Ожидаемое расстояние поезки';

comment on column approvals.final_trip_approvals.human_readable_id is 'Удобный для человека номер';

comment on column approvals.final_trip_approvals.passenger_count is 'Количество пассажиров';

comment on column approvals.final_trip_approvals.reason is 'Причина отмены';

comment on column approvals.final_trip_approvals.waypoints is 'Точки поездки';

comment on column approvals.final_trip_approvals.public_compensation_type is 'Тип компенсации';

comment on column approvals.final_trip_approvals.tariff_id is 'ID тарифа';



create index ft_approval_actor_id_idx
    on approvals.final_trip_approvals (actor_id);

create index ft_approval_action_id_idx
    on approvals.final_trip_approvals (action_id);

create index ft_approval_desired_date_idx
    on approvals.final_trip_approvals (desired_date);

create table approvals.update_trip_request_approvals
(
    id                       uuid                     not null
        constraint update_trip_request_approvals_pkey
            primary key,
    author_id                uuid                     not null,
    actor_id                 uuid                     not null,
    action_id                uuid                     not null,
    status                   text default 'NEW'::text not null,
    creation_time            timestamp                not null,
    end_time                 timestamp,
    deadline                 timestamp,
    approved_by_id           uuid,
    transport_type           text                     not null,
    trip_class               text,
    desired_date             timestamp                not null,
    trip_purpose_id          uuid                     not null,
    expected_cost            double precision,
    expected_time            bigint,
    expected_distance        double precision,
    human_readable_id        varchar(255),
    passenger_count          integer,
    reason                   text,
    waypoints                jsonb,
    update_id                uuid
        constraint utr_update_id_uk
            unique,
    public_compensation_type text,
    tariff_id                uuid
);

comment on table approvals.update_trip_request_approvals is 'Таблица согласований';

comment on column approvals.update_trip_request_approvals.id is 'Идентификатор согласований';

comment on column approvals.update_trip_request_approvals.author_id is 'Идентификатор автора';

comment on column approvals.update_trip_request_approvals.actor_id is 'Идентификатор участника';

comment on column approvals.update_trip_request_approvals.action_id is 'Идентификатор сущности согласования';

comment on column approvals.update_trip_request_approvals.status is 'Статус согласования (NEW, EDITED, ACCEPTED, DECLINED, CANCELLED)';

comment on column approvals.update_trip_request_approvals.creation_time is 'Время создания';

comment on column approvals.update_trip_request_approvals.deadline is 'Контрольный срок';

comment on column approvals.update_trip_request_approvals.approved_by_id is 'Согласующий';

comment on column approvals.update_trip_request_approvals.transport_type is 'Тип транспорта';

comment on column approvals.update_trip_request_approvals.trip_class is 'Класс такси';

comment on column approvals.update_trip_request_approvals.desired_date is 'Дата поездки';

comment on column approvals.update_trip_request_approvals.trip_purpose_id is 'Цель поездки';

comment on column approvals.update_trip_request_approvals.expected_cost is 'Ожидаемая стоиомсть поезки';

comment on column approvals.update_trip_request_approvals.expected_time is 'Ожидаемая длительность поезки';

comment on column approvals.update_trip_request_approvals.expected_distance is 'Ожидаемое расстояние поезки';

comment on column approvals.update_trip_request_approvals.human_readable_id is 'Удобный для человека номер';

comment on column approvals.update_trip_request_approvals.passenger_count is 'Количество пассажиров';

comment on column approvals.update_trip_request_approvals.reason is 'Причина отмены';

comment on column approvals.update_trip_request_approvals.waypoints is 'Точки поездки';

comment on column approvals.update_trip_request_approvals.update_id is 'Id изменения маршрута';

comment on column approvals.update_trip_request_approvals.public_compensation_type is 'Тип компенсации';

comment on column approvals.update_trip_request_approvals.tariff_id is 'ID тарифа';



create index utr_approval_actor_id_idx
    on approvals.update_trip_request_approvals (actor_id);

create index utr_approval_action_id_idx
    on approvals.update_trip_request_approvals (action_id);

create index utr_approval_desired_date_idx
    on approvals.update_trip_request_approvals (desired_date);

create table approvals.request_document
(
    document_id uuid not null
        constraint request_document_pkey
            primary key,
    request_id  uuid not null,
    employee_id uuid not null
);

comment on table approvals.request_document is 'Таблица документов заявок';

comment on column approvals.request_document.document_id is 'Идентификатор строки равный идентификатору документа';

comment on column approvals.request_document.request_id is 'Идентификатор заявки';

comment on column approvals.request_document.employee_id is 'Идентификатор сотрудника, загрузившего документ';



create index rd_document_id_idx
    on approvals.request_document (document_id);

create index rd_request_id_idx
    on approvals.request_document (request_id);

create table approvals.view_document
(
    id          uuid      not null
        constraint view_document_pkey
            primary key,
    document_id uuid      not null,
    employee_id uuid      not null,
    date_time   timestamp not null
);

comment on table approvals.view_document is 'Таблица просмотра документов заявок';

comment on column approvals.view_document.document_id is 'Идентификатор документа';

comment on column approvals.view_document.employee_id is 'Идентификатор сотрудника, просмотревшего документ';

comment on column approvals.view_document.date_time is 'Дата и время просмотра документа';



create index vd_document_id_idx
    on approvals.view_document (document_id);

create table approvals.approvers
(
    department_id  uuid not null,
    employee_id    uuid not null,
    transport_type text,
    constraint approvers_unique
        unique (department_id, employee_id, transport_type)
);

comment on column approvals.approvers.department_id is 'Подразделение, заявки которого разрешено согласовывать сотруднику employee_id';

comment on column approvals.approvers.employee_id is 'Сотрудник, имеющий право согласовывать заявки подразделения department_id';

comment on column approvals.approvers.transport_type is 'Тип транспорта. Заявки только данного типа транспорта можно согласовывать сотруднику employee_id';



create table approvals.shared_ride_approvals
(
    id                       uuid                     not null
        constraint shared_ride_approvals_pkey
            primary key,
    author_id                uuid                     not null,
    actor_id                 uuid                     not null,
    action_id                uuid                     not null,
    status                   text default 'NEW'::text not null,
    creation_time            timestamp                not null,
    end_time                 timestamp,
    deadline                 timestamp,
    approved_by_id           uuid,
    transport_type           text                     not null,
    trip_purpose_id          uuid                     not null,
    trip_class               text,
    desired_date             timestamp                not null,
    expected_cost            double precision,
    expected_time            bigint,
    expected_distance        double precision,
    human_readable_id        varchar(255),
    passenger_count          integer,
    reason                   text,
    waypoints                jsonb,
    public_compensation_type text,
    add_request_id           uuid,
    tariff_id                uuid
);

comment on table approvals.shared_ride_approvals is 'Таблица согласований';

comment on column approvals.shared_ride_approvals.id is 'Идентификатор согласований';

comment on column approvals.shared_ride_approvals.author_id is 'Идентификатор автора';

comment on column approvals.shared_ride_approvals.actor_id is 'Идентификатор участника';

comment on column approvals.shared_ride_approvals.action_id is 'Идентификатор сущности согласования';

comment on column approvals.shared_ride_approvals.status is 'Статус согласования (NEW, EDITED, ACCEPTED, DECLINED, CANCELLED)';

comment on column approvals.shared_ride_approvals.creation_time is 'Время создания';

comment on column approvals.shared_ride_approvals.deadline is 'Контрольный срок';

comment on column approvals.shared_ride_approvals.approved_by_id is 'Согласующий';

comment on column approvals.shared_ride_approvals.transport_type is 'Тип транспорта';

comment on column approvals.shared_ride_approvals.trip_purpose_id is 'Цель поездки';

comment on column approvals.shared_ride_approvals.trip_class is 'Класс такси';

comment on column approvals.shared_ride_approvals.desired_date is 'Дата поездки';

comment on column approvals.shared_ride_approvals.expected_cost is 'Ожидаемая стоиомсть поезки';

comment on column approvals.shared_ride_approvals.expected_time is 'Ожидаемая длительность поезки';

comment on column approvals.shared_ride_approvals.expected_distance is 'Ожидаемое расстояние поезки';

comment on column approvals.shared_ride_approvals.human_readable_id is 'Удобный для человека номер';

comment on column approvals.shared_ride_approvals.passenger_count is 'Количество пассажиров';

comment on column approvals.shared_ride_approvals.reason is 'Причина отмены';

comment on column approvals.shared_ride_approvals.waypoints is 'Точки поездки';

comment on column approvals.shared_ride_approvals.public_compensation_type is 'Тип компенсации';

comment on column approvals.shared_ride_approvals.add_request_id is 'Добавляемая поездка к совместной';

comment on column approvals.shared_ride_approvals.tariff_id is 'ID тарифа';



create index sr_approval_actor_id_idx
    on approvals.shared_ride_approvals (actor_id);

create index sr_approval_action_id_idx
    on approvals.shared_ride_approvals (action_id);

create index sr_approval_desired_date_idx
    on approvals.shared_ride_approvals (desired_date);

create table approvals.trip_purpose
(
    id    uuid         not null
        constraint trip_purpose_pkey
            primary key,
    label varchar(255) not null
);

comment on table approvals.trip_purpose is 'Цели поездки';

comment on column approvals.trip_purpose.id is 'Идентификатор цели';

comment on column approvals.trip_purpose.label is 'Название цели';



create table approvals.approvals_settings
(
    setting_type                       varchar(31) not null,
    id                                 uuid        not null
        constraint approvals_settings_pkey
            primary key,
    approval_active                    boolean,
    min_cost_to_be_approved            integer     not null,
    transport_type                     varchar(50) not null,
    intermediate_address_check_in      boolean,
    intermediate_address_check_in_mode varchar(50),
    trip_approval_active               boolean,
    affirmative_active                 boolean,
    approval_document_check            boolean,
    trip_confirmation_active           boolean,
    trip_confirmation_document_check   boolean,
    organization_id                    uuid        not null
        constraint approvals_settings_organization_id_fkey
            references approvals.message_organization,
    constraint approvals_settings_organization_id_transport_type_key
        unique (organization_id, transport_type)
);

comment on table approvals.approvals_settings is 'Настройки согласований';

comment on column approvals.approvals_settings.id is 'Уникальный идентификатор согласования';

comment on column approvals.approvals_settings.approval_active is 'Необходимость этапа согласования';

comment on column approvals.approvals_settings.min_cost_to_be_approved is 'Сумма, не требующая согласования';

comment on column approvals.approvals_settings.transport_type is 'Тип транспорта';

comment on column approvals.approvals_settings.intermediate_address_check_in is 'Необходимость чек-ина в промежуточных точках (все виды транспорта кроме такси и общественного)';

comment on column approvals.approvals_settings.intermediate_address_check_in_mode is 'Режим чек-ина в промежуточных точках (все виды транспорта кроме такси и общественного)';

comment on column approvals.approvals_settings.trip_approval_active is 'Необходимость этапа утверждения поездки (все виды транспорта кроме такси и общественного)';

comment on column approvals.approvals_settings.affirmative_active is 'Необходимость этапа утверждения (общественный транспорт)';

comment on column approvals.approvals_settings.approval_document_check is 'Необходимость проверки документа на этапе "Создание" (общественный транспорт)';

comment on column approvals.approvals_settings.trip_confirmation_active is 'Необходимость этапа подтверждения (общественный транспорт)';

comment on column approvals.approvals_settings.trip_confirmation_document_check is 'Необходимость проверки документа на этапе "Подтверждение поездки" (общественный транспорт)';

comment on column approvals.approvals_settings.organization_id is 'Уникальный идентификатор организации';



create table approvals.purpose_and_region_items
(
    setting_id              uuid    not null
        constraint purpose_and_region_items_setting_id_fkey
            references approvals.approvals_settings,
    min_cost_to_be_approved integer not null,
    region                  uuid,
    trip_purpose_id         uuid    not null
        constraint purpose_and_region_items_trip_purpose_id_fkey
            references approvals.trip_purpose
);

comment on table approvals.purpose_and_region_items is 'Цели и регионы поездок';

comment on column approvals.purpose_and_region_items.setting_id is 'Уникальный идентификатор настройки согласования';

comment on column approvals.purpose_and_region_items.min_cost_to_be_approved is 'Сумма, не требующая согласоавния';

comment on column approvals.purpose_and_region_items.region is 'Идентефикатор геозоны';

comment on column approvals.purpose_and_region_items.trip_purpose_id is 'Уникальный идентификатор цели поездки';



create table approvals.messages_geo_zone
(
    id        uuid   not null
        constraint geo_zone_pkey
            primary key,
    code      bigint not null,
    name      text   not null,
    parent_id uuid
);

comment on column approvals.messages_geo_zone.id is 'Идентификатор';

comment on column approvals.messages_geo_zone.code is 'Код геозоны';

comment on column approvals.messages_geo_zone.name is 'Название геозоны';

comment on column approvals.messages_geo_zone.parent_id is 'Идентификатор родительской геозоны';



create table approvals.tariff_message
(
    id                uuid not null
        constraint tariff_pkey
            primary key,
    humanreadableid   varchar(50),
    organization_id   uuid,
    region_id         uuid,
    transport_type_id uuid,
    contract_id       uuid
);

comment on table approvals.tariff_message is 'Тариф, получаемый из сообщения';

comment on column approvals.tariff_message.id is 'ID';

comment on column approvals.tariff_message.humanreadableid is 'Человекочитаемый ID';

comment on column approvals.tariff_message.organization_id is 'ID корп.клиента - владельца тарифа';

comment on column approvals.tariff_message.region_id is 'ID геозоны';

comment on column approvals.tariff_message.transport_type_id is 'ID типа транспорта';

comment on column approvals.tariff_message.contract_id is 'ID контракта';



create table approvals.taxi_tariff_message
(
    id              uuid not null
        constraint taxi_tariff_pkey
            primary key,
    humanreadableid varchar(50),
    region_id       uuid,
    service_type    varchar(100),
    organization_id uuid,
    transport_type  varchar(50),
    contract_id     uuid,
    taxi_class      varchar(50)
);

comment on table approvals.taxi_tariff_message is 'Тариф такси, получаемый из сообщения';

comment on column approvals.taxi_tariff_message.id is 'ID';

comment on column approvals.taxi_tariff_message.humanreadableid is 'Человекочитаемый ID';

comment on column approvals.taxi_tariff_message.region_id is 'ID геозоны';

comment on column approvals.taxi_tariff_message.service_type is 'Вид транспортной услуги';

comment on column approvals.taxi_tariff_message.organization_id is 'ID корп.клиента';

comment on column approvals.taxi_tariff_message.transport_type is 'Тип транспорта';

comment on column approvals.taxi_tariff_message.contract_id is 'ID договора';

comment on column approvals.taxi_tariff_message.taxi_class is 'Класс такси';