-- ============================================
-- 1. Создание ENUM типа для type колонки
-- ============================================
CREATE TYPE approvals.approval_journal_type AS ENUM (
    'UPDATE_TRIP_REQUEST',
    'TRIP_REQUEST',
    'FINAL_TRIP',
    'SHARED_RIDE_JOIN'
);

-- ============================================
-- 2. Добавление таблицы approval_journal
-- ============================================
CREATE TABLE approvals.approval_journal (
    id uuid NOT NULL, -- Идентификатор записи
    approval_id uuid NOT NULL, -- Идентификатор записи о согласовании
    actor_id uuid NOT NULL, -- Идентификатор участника
    action_id uuid NOT NULL, -- Идентификатор сущности
    "status" approvals.approval_status DEFAULT 'NEW'::approvals.approval_status NOT NULL, -- Статус согласования (NEW, EDITED, ACCEPTED, DECLINED, CANCELLED)
    creation_time timestamp NOT NULL, -- Дата и время создания согласования
    approved_by_id uuid NULL, -- Идентификатор сотрудника, выполнившего согласование
    transport_type text NULL, -- Тип транспорта
    taxi_class text NULL, -- Класс такси
    desired_date timestamp NULL, -- Желаемая дата и время поездки
    trip_purpose_id uuid NULL, -- Идентификатор цели поездки
    expected_cost float8 NULL, -- Ожидаемая стоимость поездки
    expected_time int8 NULL, -- Ожидаемое время в пути (в секундах)
    expected_distance float8 NULL, -- Ожидаемая дистанция поездки
    human_readable_id varchar(255) NULL, -- Человеко-читаемый идентификатор согласования
    passenger_count int4 NULL, -- Количество пассажиров
    waypoints jsonb NULL, -- Путевые точки в формате JSON
    shared_ride_id uuid NULL, -- Идентификатор совместной поездки
    add_request_id uuid NULL, -- Идентификатор заявки, добавленной к поездке
    time_zone text NULL, -- Временная зона
    type approvals.approval_journal_type NOT NULL -- Тип записи журнала (UPDATE_TRIP_REQUEST, TRIP_REQUEST, FINAL_TRIP, SHARED_RIDE_JOIN)
);

-- Комментарии к таблице
COMMENT ON TABLE approvals.approval_journal IS 'Журнал согласований';

-- Комментарии к колонкам
COMMENT ON COLUMN approvals.approval_journal.id IS 'Идентификатор записи';
COMMENT ON COLUMN approvals.approval_journal.approval_id IS 'Идентификатор записи о согласовании';
COMMENT ON COLUMN approvals.approval_journal.actor_id IS 'Идентификатор участника';
COMMENT ON COLUMN approvals.approval_journal.action_id IS 'Идентификатор сущности';
COMMENT ON COLUMN approvals.approval_journal.status IS 'Статус согласования (NEW, EDITED, ACCEPTED, DECLINED, CANCELLED)';
COMMENT ON COLUMN approvals.approval_journal.creation_time IS 'Дата и время создания согласования';
COMMENT ON COLUMN approvals.approval_journal.approved_by_id IS 'Идентификатор сотрудника, выполнившего согласование';
COMMENT ON COLUMN approvals.approval_journal.transport_type IS 'Тип транспорта';
COMMENT ON COLUMN approvals.approval_journal.taxi_class IS 'Класс такси';
COMMENT ON COLUMN approvals.approval_journal.desired_date IS 'Желаемая дата и время поездки';
COMMENT ON COLUMN approvals.approval_journal.trip_purpose_id IS 'Идентификатор цели поездки';
COMMENT ON COLUMN approvals.approval_journal.expected_cost IS 'Ожидаемая стоимость поездки';
COMMENT ON COLUMN approvals.approval_journal.expected_time IS 'Ожидаемое время в пути (в секундах)';
COMMENT ON COLUMN approvals.approval_journal.expected_distance IS 'Ожидаемая дистанция поездки';
COMMENT ON COLUMN approvals.approval_journal.human_readable_id IS 'Человеко-читаемый идентификатор согласования';
COMMENT ON COLUMN approvals.approval_journal.passenger_count IS 'Количество пассажиров';
COMMENT ON COLUMN approvals.approval_journal.waypoints IS 'Путевые точки маршрута в формате JSON';
COMMENT ON COLUMN approvals.approval_journal.shared_ride_id IS 'Идентификатор совместной поездки';
COMMENT ON COLUMN approvals.approval_journal.add_request_id IS 'Идентификатор заявки, добавленной к поездке';
COMMENT ON COLUMN approvals.approval_journal.time_zone IS 'Временная зона';
COMMENT ON COLUMN approvals.approval_journal.type IS 'Тип записи журнала (UPDATE_TRIP_REQUEST, TRIP_REQUEST, FINAL_TRIP, SHARED_RIDE_JOIN)';