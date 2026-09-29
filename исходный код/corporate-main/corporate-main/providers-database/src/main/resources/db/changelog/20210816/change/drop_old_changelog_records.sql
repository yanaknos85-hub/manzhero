do
$$
    begin
        if (select exists(select *
                          from information_schema.tables
                          where table_schema = 'public' and table_name = 'databasechangelog'))
        then
            delete
            from public.databasechangelog
            where id ilike 'corporate/%'
               or id ilike '%/corporate'
               or id ilike 'authomatic-corporate/%';
        end if;
    end;
$$;