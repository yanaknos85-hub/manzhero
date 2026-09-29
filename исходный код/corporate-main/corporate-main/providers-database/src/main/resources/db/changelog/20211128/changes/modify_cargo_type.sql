drop table corporate.cargo_type_nomenclature;

delete from corporate.cargo_type;
alter table corporate.cargo_type add name varchar(128) not null;
alter table corporate.cargo_type add type varchar(128) not null;
alter table corporate.cargo_type add category varchar(128) not null;
alter table corporate.cargo_type add length float8;
alter table corporate.cargo_type add width float8;
alter table corporate.cargo_type add height float8;
alter table corporate.cargo_type add weight float8;
alter table corporate.cargo_type add volume float8;
alter table corporate.cargo_type drop label;
alter table corporate.cargo_type drop active;

create unique index cargo_type_name_uindex
    on corporate.cargo_type (name);

comment on table corporate.cargo_type is 'Справочник типов грузов';
comment on column corporate.cargo_type.name is 'Название';
comment on column corporate.cargo_type.type is 'Вид';
comment on column corporate.cargo_type.category is 'Категория';
comment on column corporate.cargo_type.length is 'Длина';
comment on column corporate.cargo_type.width is 'Ширина';
comment on column corporate.cargo_type.height is 'Высота';
comment on column corporate.cargo_type.weight is 'Вес';
comment on column corporate.cargo_type.volume is 'Объем';