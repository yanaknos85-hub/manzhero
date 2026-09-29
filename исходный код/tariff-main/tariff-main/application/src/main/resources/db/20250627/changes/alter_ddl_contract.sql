alter table tariff_fleet.contract
    add active boolean default false not null;

update tariff_fleet.contract c
set "end" = start + INTERVAL '1 YEAR'
where "end" is null;

alter table tariff_fleet.contract
    alter column "end" set not null;

comment on column tariff_fleet.contract.active is 'Флаг активности';

update tariff_fleet.contract c
set active = (case when current_date between c.start and c."end" then true else false end);

alter table tariff_fleet.contract
    alter column active drop default;