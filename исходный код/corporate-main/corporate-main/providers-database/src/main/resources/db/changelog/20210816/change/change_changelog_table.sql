do
$$
    begin
        if (select exists(select *
                          from information_schema.tables
                          where table_catalog = 'transport' and table_schema = 'public' and table_name = 'databasechangelog'))
        then
            insert into
                public.changelog_corporate
            select
                *
            from
                public.databasechangelog
            where
                    id ilike 'corporate/%' or id ilike '%/corporate'
               or id ilike 'authomatic-corporate/%'
            ;

            update public.changelog_corporate
            set filename = 'classpath:db/changelog/20200322/changelog.yml',
                md5sum = '8:718399638ddffc468cbe3955937a3a30'
            where id = '20200322/corporate';
            update public.changelog_corporate
            set filename = 'classpath:db/changelog/20200409/changelog.yml',
                md5sum = '8:ce12fa0bf2d8e9315d97c7b75fd799f6'
            where id = '20200409/corporate';
        end if;
    end;
$$;