-- Сервис избранных маршрутов — отдельная схема БД.
-- Черновик создания схемы нового сервиса, не готовая миграция для внедрения.
CREATE SCHEMA IF NOT EXISTS favorite_route;

CREATE TABLE favorite_route.favorite_route (
    -- Идентификатор генерирует приложение, как в сервисе тарифов
    id                 uuid PRIMARY KEY,
    favorite_route_number varchar(20) NOT NULL UNIQUE,
    route_name         varchar(255) NOT NULL,
    -- jsonb предлагается вместо json из БТ §10.1 (согласует архитектор)
    waypoints          jsonb        NOT NULL,
    -- Значения вида: TAXI, GROUP_TRANSFER
    transport_type     varchar(50)  NOT NULL,
    -- Код класса должен соответствовать
    -- значению transport_type
    service_class      varchar(50)  NOT NULL,
    -- D06: расчётные значения обязательны, маршрут сохраняется после расчёта
    total_km           numeric(7,3) NOT NULL,
    -- Время в пути без учёта ожидания и с учётом, минуты
    travel_time        numeric(6,1) NOT NULL,
    total_time         numeric(6,1) NOT NULL,
    -- Денежные значения в копейках, целым числом
    recommended_cost   int4         NOT NULL,
    fixed_cost         int4         NOT NULL,
    is_night_tariff    boolean      NOT NULL DEFAULT false,
    valid_from         date,
    valid_to           date,
    is_active          boolean      NOT NULL DEFAULT true,
    -- Идентификаторы внешних сервисов: внешние ключи не создаются,
    -- целостность обеспечивается приложением
    tariff_id          uuid         NOT NULL,
    -- UUID организации
    organization_id    uuid         NOT NULL,
    created_by         uuid         NOT NULL,
    updated_by         uuid         NOT NULL,
    created_at         timestamptz  NOT NULL DEFAULT now(),
    updated_at         timestamptz  NOT NULL DEFAULT now(),
    -- Оптимистичная блокировка (JPA @Version, тип Long); номер пишется в историю
    version            int8         NOT NULL DEFAULT 0,

    CONSTRAINT chk_fixed_cost_positive CHECK (fixed_cost > 0),
    -- Предложение (П19): неотрицательность и согласованность расчётных полей
    CONSTRAINT chk_recommended_cost_nonneg CHECK (recommended_cost >= 0),
    CONSTRAINT chk_total_km_nonneg CHECK (total_km >= 0),
    CONSTRAINT chk_travel_time_nonneg CHECK (travel_time >= 0),
    CONSTRAINT chk_total_time_ge_travel CHECK (total_time >= travel_time),
    -- D10: максимум 10 подтверждён; минимум 3 (база, остановка, база) — согласовать с заказчиком.
    CONSTRAINT chk_waypoints_count
        CHECK (CASE WHEN jsonb_typeof(waypoints) = 'array'
                    THEN jsonb_array_length(waypoints) BETWEEN 3 AND 10
                    ELSE false END),
    CONSTRAINT chk_valid_period
        CHECK (valid_to IS NULL OR valid_from IS NULL
               OR valid_from <= valid_to)
);

comment on column favorite_route.favorite_route.id
    is 'Уникальный идентификатор записи (первичный ключ)';
comment on column favorite_route.favorite_route.favorite_route_number
    is 'Формат FR-XXXX-XXXXXXXX: XXXX = digitId организации';
comment on column favorite_route.favorite_route.waypoints
    is 'Точки по порядку: база (выезд), остановки с ожиданием, база (возврат). Элемент: country, region, latitude, longitude обязательны; city, street, house, building, structure необязательны; waitTime — int64, миллисекунды, только у промежуточных точек.';
comment on column favorite_route.favorite_route.transport_type
    is 'Вид транспорта: такси / трансфер';
comment on column favorite_route.favorite_route.service_class
    is 'Класс услуги';
comment on column favorite_route.favorite_route.total_km
    is 'Протяжённость маршрута, км';
comment on column favorite_route.favorite_route.travel_time
    is 'Время в пути без учёта ожидания, мин';
comment on column favorite_route.favorite_route.total_time
    is 'Время в маршруте с учётом ожидания, мин';
comment on column favorite_route.favorite_route.recommended_cost
    is 'Расчётная стоимость от тарифа, коп';
comment on column favorite_route.favorite_route.fixed_cost
    is 'Фиксированная стоимость по прайсу, коп';
comment on column favorite_route.favorite_route.is_night_tariff
    is 'Признак ночного тарифа, 22:00-06:00';
comment on column favorite_route.favorite_route.valid_from
    is 'Начало периода действия маршрута';
comment on column favorite_route.favorite_route.valid_to
    is 'Окончание периода действия маршрута';
comment on column favorite_route.favorite_route.tariff_id
    is 'Идентификатор тарифа в сервисе тарифов (снимок на момент расчёта recommended_cost)';
comment on column favorite_route.favorite_route.organization_id
    is 'UUID организации';

comment on column favorite_route.favorite_route.route_name
    is 'Наименование маршрута';
comment on column favorite_route.favorite_route.is_active
    is 'Признак активности записи';
comment on column favorite_route.favorite_route.created_by
    is 'Автор записи (UUID пользователя)';
comment on column favorite_route.favorite_route.updated_by
    is 'Пользователь, выполнивший последнее сохранение; при создании — автор';
comment on column favorite_route.favorite_route.created_at
    is 'Дата и время создания записи';
comment on column favorite_route.favorite_route.updated_at
    is 'Дата и время последнего изменения';
comment on column favorite_route.favorite_route.version
    is 'Версия записи для оптимистичной блокировки';

-- Предложение (П6): основной запрос — маршруты организации, только активные.
-- Индекс создаётся в первой миграции: таблица пуста, блокировки записи нет.
CREATE INDEX idx_favorite_route_org_active
    ON favorite_route.favorite_route (organization_id, is_active);

-- История изменений: EAV по БТ §10.2 с номером версии записи (D04).
CREATE TABLE favorite_route.favorite_route_history (
    id                 uuid PRIMARY KEY,
    favorite_route_id  uuid NOT NULL
        CONSTRAINT favorite_route_history_route_fk
        REFERENCES favorite_route.favorite_route (id),
    route_version      int8 NOT NULL,
    changed_by         uuid NOT NULL,
    changed_at         timestamptz NOT NULL DEFAULT now(),
    field_name         varchar(64) NOT NULL,
    old_value          text,
    new_value          text,
    CONSTRAINT favorite_route_history_uq
        UNIQUE (favorite_route_id, field_name, route_version),
    CONSTRAINT favorite_route_history_field_chk
        CHECK (field_name IN ('route_name', 'waypoints', 'transport_type', 'service_class',
                              'total_km', 'travel_time', 'total_time', 'recommended_cost',
                              'fixed_cost', 'is_night_tariff', 'valid_from', 'valid_to',
                              'is_active', 'tariff_id'))
);

comment on column favorite_route.favorite_route_history.field_name
    is 'Имя изменённого поля: колонка favorite_route в snake_case';
comment on column favorite_route.favorite_route_history.old_value
    is 'Значение до изменения, текстовое представление';
comment on column favorite_route.favorite_route_history.new_value
    is 'Значение после изменения, текстовое представление';

comment on column favorite_route.favorite_route_history.id
    is 'UUID записи истории, генерируется приложением';
comment on column favorite_route.favorite_route_history.favorite_route_id
    is 'Ссылка на запись справочника';
comment on column favorite_route.favorite_route_history.changed_by
    is 'UUID пользователя, выполнившего изменение';
comment on column favorite_route.favorite_route_history.changed_at
    is 'Дата и время изменения';
comment on column favorite_route.favorite_route_history.route_version
    is 'Версия записи справочника после сохранения (тот же тип, что favorite_route.version)';

-- Предложение (П2): служебная таблица счётчика номера FR-XXXX-XXXXXXXX.
-- Образец — request.company_sq в сервисе заявок; нужна библиотеке human-readable-generator.
CREATE TABLE favorite_route.company_sq (
    id          uuid        PRIMARY KEY,
    prefix      varchar(2)  NOT NULL,
    orgdigitid  numeric     NOT NULL,
    sq          numeric     NOT NULL,
    dt_insert   timestamptz NOT NULL DEFAULT now(),
    dt_modify   timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_company_sq_1
    ON favorite_route.company_sq (prefix, orgdigitid, sq);
CREATE UNIQUE INDEX ux_company_sq_2
    ON favorite_route.company_sq (prefix, orgdigitid);

comment on table favorite_route.company_sq
    is 'Счётчик номеров по организациям для favorite_route_number';
comment on column favorite_route.company_sq.id
    is 'Уникальный идентификатор (первичный ключ)';
comment on column favorite_route.company_sq.prefix
    is 'Код сущности (FR)';
comment on column favorite_route.company_sq.orgdigitid
    is 'Числовой код организации (digitId, 1-9999)';
comment on column favorite_route.company_sq.sq
    is 'Последний выданный порядковый номер в рамках префикса и организации';
comment on column favorite_route.company_sq.dt_insert
    is 'Дата и время вставки записи';
comment on column favorite_route.company_sq.dt_modify
    is 'Дата и время модификации записи';
