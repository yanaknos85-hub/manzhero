alter table corporate.position add column active bool default true;

comment on column corporate.position.active is 'Флаг активности'