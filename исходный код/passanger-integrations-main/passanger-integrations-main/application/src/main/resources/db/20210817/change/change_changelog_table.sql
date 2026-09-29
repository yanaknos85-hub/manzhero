do
$$
    begin
        if (select exists(select *
                          from information_schema.tables
                          where table_catalog = 'transport' and table_schema = 'public' and table_name = 'databasechangelog'))
        then
            insert into
                public.changelog_integrations
            select
                *
            from
                public.databasechangelog
            where
                    id ilike 'integrations/%' or id ilike '%/integrations'
               or id ilike 'authomatic-integrations/%';
        end if;
    end;
$$;