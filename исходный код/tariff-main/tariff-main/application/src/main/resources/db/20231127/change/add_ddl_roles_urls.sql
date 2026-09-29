create table tariff_fleet.urls
(
    id      uuid not null
        constraint users_urls_pkey
            primary key,
    url     text not null,
    pattern text not null,
    method  text not null
);

comment
    on table tariff_fleet.urls is 'Ссылка';

comment
    on column tariff_fleet.urls.id is 'Идентификатор записи ссылки';

comment
    on column tariff_fleet.urls.url is 'Адрес ссылки';

comment
    on column tariff_fleet.urls.pattern is 'Паттерн ссылки';

comment
    on column tariff_fleet.urls.method is 'Наименование REST метода';

create table tariff_fleet.roles
(
    role   text not null,
    url_id uuid not null
        constraint role_urls_fkey
            references tariff_fleet.urls,
    constraint user_roles_ukey
        unique (url_id, role)
);

comment
    on table tariff_fleet.roles is 'Связка роль-ссылка';

comment
    on column tariff_fleet.roles.role is 'Наименование роли';

comment
    on column tariff_fleet.roles.url_id is 'Идентификатор записи о ссылке';