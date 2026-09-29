create table limits.bonus_request
(
    id            uuid                    not null
        constraint bonus_request_pk
            primary key,
    bonus         uuid                    not null
        constraint bonus_request_bonus_bonus_owner_id_fk
            references limits.bonus,
    sum           bigint                  not null,
    operation     text                    not null,
    creation_time timestamp default now() not null,
    status        text                    not null,
    update_time   timestamp default now() not null,
    reason        text
);

comment
on table limits.bonus_request is 'Бонусный счёт. Запросы';

comment
on column limits.bonus_request.bonus is 'Бонусный счёт';

comment
on column limits.bonus_request.sum is 'Сумма';

comment
on column limits.bonus_request.operation is 'Операция со счётом';

comment
on column limits.bonus_request.creation_time is 'Дата создания запроса';

comment
on column limits.bonus_request.status is 'Статус запроса';

comment
on column limits.bonus_request.update_time is 'Время обновления запроса';

comment
on column limits.bonus_request.reason is 'Причина';