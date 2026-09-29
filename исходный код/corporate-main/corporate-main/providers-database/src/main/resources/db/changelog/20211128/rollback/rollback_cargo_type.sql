delete from corporate.cargo_type;
alter table corporate.cargo_type drop name;
alter table corporate.cargo_type drop type;
alter table corporate.cargo_type drop category;
alter table corporate.cargo_type drop length;
alter table corporate.cargo_type drop width;
alter table corporate.cargo_type drop height;
alter table corporate.cargo_type drop weight;
alter table corporate.cargo_type drop volume;
alter table corporate.cargo_type add label varchar(128) NOT NULL;
alter table corporate.cargo_type add active boolean default true not null;

comment on table corporate.cargo_type is 'Справочник типов грузов';
comment on column corporate.cargo_type.id is 'Идентификатор';
comment on column corporate.cargo_type.label is 'Название типа груза';
comment on column corporate.cargo_type.active is 'Признак активной записи';

CREATE TABLE corporate.cargo_type_nomenclature
(
    id              uuid PRIMARY KEY,
    cargo_type_id   uuid NOT NULL
        CONSTRAINT cargo_type_nomenclature_type_fk REFERENCES corporate.cargo_type (id),
    label           varchar not null
);

comment on table corporate.cargo_type_nomenclature is 'Справочник номенклатуры типа груза';
comment on column corporate.cargo_type_nomenclature.id is 'Идентификатор номенклатуры';
comment on column corporate.cargo_type_nomenclature.id is 'Идентификатор типа груза';
comment on column corporate.cargo_type_nomenclature.label is 'Описание номенклатуры';