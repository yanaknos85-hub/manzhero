do
$$
    begin
        if (select exists(select *
                          from information_schema.tables
                          where table_catalog = 'transport' and table_schema = 'public' and table_name = 'databasechangelog'))
        then
            if (select exists(select * from public.databasechangelog where id ilike '%limit%')) then
                insert into public.changelog_limits (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments,
                                                liquibase, deployment_id)
                values ('20211222-1', 'medvedev-ad', 'classpath:db/20211222/changelog.yml', now(), 2, 'EXECUTED',
                        '8:f3119890d57fd6e9bc111d500afaa0a4', 'sqlFile', '', '3.8.9', 1);

                insert into public.changelog_limits select * from public.databasechangelog where id like 'authomatic-limit*';
            end if;
        end if;
    end;
$$;