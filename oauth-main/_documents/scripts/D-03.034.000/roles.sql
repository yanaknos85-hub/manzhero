do
$$
    declare
        item information_schema.tables%rowtype;
        stmt text;
        insert_stmt text;
        target_role text = 'ROLE_PARKING_ADMIN_ORGANIZATION';
        source_role text = 'ROLE_PARKING_ADMIN';
        role authentication.roles%rowtype;
    BEGIN
        for item in select * from information_schema.tables
            where table_name = 'roles' loop
            stmt = 'select * from "' || item.table_schema || '"."roles" where "role" = ''' || source_role || ''';';
            begin
                for role in execute stmt loop
                    insert_stmt = 'insert into "' || item.table_schema || '"."' || item.table_name || '" ("url_id", "role") values (''' || role.url_id || ''', ''' || target_role || ''') on conflict do nothing;';
                    execute insert_stmt;
                end loop;
            exception when others
            then
              raise notice '%', stmt;
            end;
        end loop;
    end;
$$;