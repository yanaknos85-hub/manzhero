do
$$
    begin
        if (select exists(select *
                          from information_schema.tables
                          where table_catalog = 'transport' and table_schema = 'public' and table_name = 'databasechangelog'))
        then
            if (select exists(select * from public.databasechangelog where id ilike 'approvals/%')) then
                insert into public.changelog_approvals (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, liquibase, deployment_id)
                values ('20210905-1', 'medvedev-ad', 'classpath:db/changelog/20210905/changelog.yml', now(), 2, 'EXECUTED', '8:f00a7aa4cf17114742ef148dbe550bcd', 'sqlFile', '', '3.8.9', (select max(deployment_id)::bigint + 1 from public.databasechangelog));

                insert into public.changelog_approvals select * from public.databasechangelog where id = 'authomatic-approvals/20210520';
            end if;
        end if;
    end;
$$;