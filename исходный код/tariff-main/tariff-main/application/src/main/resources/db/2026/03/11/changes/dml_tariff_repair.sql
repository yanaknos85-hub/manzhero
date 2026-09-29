do $$
declare
    r record;
    null_count integer;
begin
    for r in
        select
            rt.tariff_id as repair_tariff_id,
            d.id as department_id
        from tariff_fleet.repair_tariff rt
        join tariff_fleet.organization o on rt.organization_id = o.id
        join tariff_fleet.department d on o.id = d.organization_id
        where d.parent_id is null
        and d.active is true
    loop
        update tariff_fleet.repair_tariff
        set department_id = r.department_id
        where tariff_id = r.repair_tariff_id;
    end loop;

    select count(*) into null_count
    from tariff_fleet.repair_tariff
    where department_id is null;

    if null_count > 0 then
        raise exception 'В таблице repair_tariff остались не заполненные поля department_id';
    end if;

    alter table if exists tariff_fleet.repair_tariff
    alter column department_id set not null;
end;
$$;
