create type migrations.column_meta as
(
    table_schema text,
    table_name   text,
    column_name  text,
    data_type    text
);
create temporary table if not exists virtual_links
(
    base_table_schema   varchar(255),
    base_table_name     varchar(255),
    base_column_name    varchar(255),
    linked_table_schema varchar(255),
    linked_table_name   varchar(255),
    linked_column_name  varchar(255)
);
create index virtual_links_idx on virtual_links (base_table_schema, base_table_name, base_column_name);

create or replace function migrations.primaries(tableData migrations.column_meta) returns text[] language plpgsql as
$$
    declare
        result text[];
    begin
        select array_agg(distinct(column_name)) into result
        from information_schema.table_constraints
            inner join information_schema.key_column_usage on key_column_usage.constraint_name = table_constraints.constraint_name
        where constraint_type = 'PRIMARY KEY' and key_column_usage.table_schema = tableData.table_schema and key_column_usage.table_name = tableData.table_name;
        return coalesce(result, array[]::text[]);
    end;
$$;

create or replace procedure migrations.delete_foreigns(curStep text, target migrations.column_meta, org text[],
                                                      schemas text[])
    language plpgsql as
$$
declare
    constraintsCount numeric;
    constraintData   migrations.column_meta;
    constraintRecord text[];
    stmt             text;
    pk             text;
begin
    select count(key_column_usage.*)
    into constraintsCount
    from information_schema.table_constraints
             inner join information_schema.constraint_column_usage
                        on table_constraints.constraint_name =
                           constraint_column_usage.constraint_name
             inner join information_schema.key_column_usage
                        on table_constraints.constraint_name = key_column_usage.constraint_name
    where constraint_type = 'FOREIGN KEY'
      and constraint_column_usage.table_schema = target.table_schema
      and constraint_column_usage.table_name = target.table_name
    group by key_column_usage.table_schema,
             key_column_usage.table_name,
             key_column_usage.column_name;
    for constraintData in select key_column_usage.table_schema,
                                 key_column_usage.table_name,
                                 key_column_usage.column_name,
                                 'uuid'
                          from information_schema.table_constraints
                                   inner join information_schema.constraint_column_usage
                                              on table_constraints.constraint_name =
                                                 constraint_column_usage.constraint_name
                                   inner join information_schema.key_column_usage
                                              on table_constraints.constraint_name = key_column_usage.constraint_name
                          where constraint_type = 'FOREIGN KEY'
                            and constraint_column_usage.table_schema = target.table_schema
                            and constraint_column_usage.table_name = target.table_name
                          group by key_column_usage.table_schema,
                                   key_column_usage.table_name,
                                   key_column_usage.column_name
        loop
            for pk in select * from unnest(migrations.primaries(target)) loop
                stmt := 'select array_agg("' || pk || '"::text) from "' || target.table_schema || '"."' || target.table_name || '" where "' || target.column_name || '"::text in (' || migrations.to_string_array(org) || ');';
                execute stmt into constraintRecord;
                call migrations.delete_data(curStep, target, constraintData, constraintRecord, schemas);
            end loop;
        end loop;
end;
$$;

create or replace procedure migrations.delete_virtuals(curStep text, target migrations.column_meta, org text[],
                                                      schemas text[])
    language plpgsql as
$$
declare
    stmt            text;
    virtualCount    numeric = 0;
    allVirtualCount integer;
    virtualLink     migrations.column_meta;
    virtual         text[];
    schemasRecord   text[];
begin
    schemasRecord := array_append(schemas, target.table_schema::text);
    select count(*)
    into virtualCount
    from virtual_links
    where base_table_schema = target.table_schema
      and base_table_name = target.table_name
      and base_column_name = target.column_name;
        if virtualCount > 0 then
        for virtualLink in select linked_table_schema, linked_table_name, linked_column_name, virtualCount
                           from virtual_links
                           where base_table_schema = target.table_schema
                             and base_table_name = target.table_name
                             and base_column_name = target.column_name and not (linked_table_schema = any (schemas)) loop
                stmt := 'select array_agg("' || virtualLink.column_name || '"::text) from "' || virtualLink.table_schema || '"."' || virtualLink.table_name || '" where "' || virtualLink.column_name || '"::text in (' || migrations.to_string_array(org) || ');';
                execute stmt into virtual;
                call migrations.delete_data(curStep, target, virtualLink, virtual, schemasRecord);
            end loop;
    else
        select count(column_name) into virtualCount
            from information_schema.columns
            where table_schema not ilike ('pg_%')
              and table_schema not in ('public', 'migrations')
              and (table_schema != target.table_schema and
                   table_name != target.table_name and not (table_schema = any (schemas)))
              and (data_type = 'uuid');
        if virtualCount > 0 then
            for virtualLink in select table_schema, table_name, column_name, data_type
                           from information_schema.columns
                           where table_schema not ilike ('pg_%')
                             and table_schema not in ('public', 'migrations')
                             and (table_schema != target.table_schema and
                                  table_name != target.table_name and not (table_schema = any (schemas)))
                             and (data_type = 'uuid')
                loop
                    stmt := 'select count(*) from "' || virtualLink.table_schema || '"."' || virtualLink.table_name ||
                            '" where "' || virtualLink.column_name || '"::text in (' || migrations.to_string_array(org) || ');';
                    execute stmt into virtualCount;
                    if (virtualCount > 0) then
                        insert into virtual_links (base_table_schema, base_table_name, base_column_name, linked_table_schema, linked_table_name, linked_column_name)
                        values (target.table_schema, target.table_name, target.column_name, virtualLink.table_schema, virtualLink.table_name, virtualLink.column_name);
                        allVirtualCount := allVirtualCount + virtualCount;
                        stmt := 'select array_agg("' || virtualLink.column_name || '"::text) from "' || virtualLink.table_schema || '"."' || virtualLink.table_name || '" where "' || virtualLink.column_name || '"::text in (' || migrations.to_string_array(org) || ');';
                        execute stmt into virtual;
                        call migrations.delete_data(curStep, target, virtualLink, virtual, schemasRecord);
                    end if;
                end loop;
        end if;
    end if;
end;
$$;

create or replace procedure migrations.delete_data(separator text, source migrations.column_meta, target migrations.column_meta, org text[], schemas text[])
    language plpgsql as
$$
begin
    declare
        processed numeric;
        _deleted numeric;
        stmt text;
    begin
        select count(*)
        into processed
        from virtual_links
        where base_table_schema = source.table_schema
          and base_table_name = source.table_name
          and base_column_name = source.column_name
          and linked_table_schema = target.table_schema
          and linked_table_name = target.table_name
          and linked_column_name = target.column_name
        ;
        if processed = 0 and org is not null then
            separator:=separator || '->' || target.table_schema || '.' || target.table_name || '.' || target.column_name;
            if not (target.table_schema = any(schemas)) then
                raise notice 'Proccessing % foreigns', separator;
                call migrations.delete_foreigns(separator, target, org, schemas);
                raise notice 'Proccessing % virtuals', separator;
                call migrations.delete_virtuals(separator, target, org, schemas);
                raise notice 'Proccessing % finished', separator;

                stmt:='with deleted as (delete from "' || target.table_schema || '"."' || target.table_name || '" where "' || target.column_name || '"::text in (' || migrations.to_string_array(org) || ') returning *) select count(*) from deleted;';
                execute stmt into _deleted;
                raise notice 'Performing: %', stmt;
                raise notice 'Proccessing % deleted. %', separator, _deleted;

            end if;
        end if;
    end;
end;
$$;

create or replace function migrations.to_string_array(source text[]) returns text language plpgsql as
$$
begin
    return replace(replace(replace(source::text, '{', ''''), '}', '''::text'), ',', '''::text,''');
end;
$$;

create or replace procedure migrations.clean(isInternal bool)
    language plpgsql as
$$
declare
    org          text[];
    source   migrations.column_meta;
    target   migrations.column_meta;
    orgCount     numeric;
    orgStep      numeric = 1;
    startTime    date;
    step text;
    _deleted numeric;
    stmt text;
BEGIN
    select count(*)
    into orgCount
    from corporate.organization
    where (isInternal and easup_id is not null) or (not isInternal and easup_id is null);

    raise notice '% organization(-s) to clean', orgCount;
    step = 'corporate.organization.id';
    select array_agg(id::text) into org from corporate.organization where
                     (isInternal and easup_id is not null)
                  or (not isInternal and easup_id is null);
-- id = '20cf17e5-23c5-4a77-90c8-805a7f3f51f5';

    startTime := now();
    raise notice 'Proccessing % Organization % from % (%pc) cleaning', step, orgStep, orgCount, orgStep / orgCount * 100;

    for target in select table_schema, table_name, column_name, data_type
                  from information_schema.columns
                  where table_schema not ilike ('pg_%')
                    and table_schema not in ('public', 'migrations')
        loop
            if (target.column_name ilike '%organization%' and target.data_type = 'uuid') or
               (target.table_name ilike '%organization%' and target.column_name = 'id') then
                select 'corporate' as table_schema, 'organization' as table_name, 'id' as column_name, 'uuid' as data_type into source;
                call migrations.delete_data(step,  source, target, org, array[]::text[]);
            end if;
        end loop;

    raise notice 'Deleting %', org;

    stmt:='with deleted as (delete from "corporate"."organization" where "id"::text in (' || migrations.to_string_array(org) || ') returning *) select count(*) from deleted;';
    execute stmt into _deleted;
    raise notice 'Performing: %', stmt;

    raise notice 'Processing % deleted. %', step, _deleted;
end;
$$;

call migrations.clean(true);

drop type if exists migrations.column_meta cascade;
drop table if exists virtual_links cascade;
drop function if exists migrations.primaries(tableData migrations.column_meta) cascade;
drop procedure if exists migrations.delete_foreigns(curStep text, target migrations.column_meta, org text[],
                                                    schemas text[]) cascade;
drop procedure if exists migrations.delete_virtuals(curStep text, target migrations.column_meta, org text[],
                                                    schemas text[]) cascade;
drop procedure if exists migrations.delete_data(separator text, source migrations.column_meta, target migrations.column_meta, org text[], schemas text[]) cascade;
drop function if exists migrations.to_string_array(source text[]) cascade;
drop procedure if exists migrations.clean(isInternal bool) cascade;