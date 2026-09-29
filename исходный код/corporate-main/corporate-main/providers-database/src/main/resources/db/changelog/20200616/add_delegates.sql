create table corporate.delegate
(
    id                uuid primary key,
    start_date        date not null,
    end_date          date not null,
    user_id           uuid not null references corporate.employee (id),
    supervisor_id     uuid not null references corporate.employee (id),
    transport_type_id uuid not null references corporate.transport_type (id)
);