alter table corporate.trip_purpose add column purpose_type varchar(255) not null default 'CORPORATE';

comment on column corporate.trip_purpose.purpose_type is 'Тип цели (Личная/Корпоративная)'