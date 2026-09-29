DO
$do$
    BEGIN
        IF EXISTS(
                SELECT routine_schema,
                       routine_name,
                       routine_type
                FROM information_schema.routines
                WHERE routine_name = 'fill_roles'
                  and routine_schema = 'migrations'
                  and routine_type = 'PROCEDURE'
            ) THEN
            EXECUTE 'call migrations.fill_roles(''tariff_fleet'', ''POST /contract/'', ''ROLE_ADMIN_DATA_MASTER'', true)';
            EXECUTE 'call migrations.fill_roles(''tariff_fleet'', ''POST /contract/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''tariff_fleet'', ''PATCH /contract/{contractId}/'', ''ROLE_ADMIN_DATA_MASTER'', true)';
            EXECUTE 'call migrations.fill_roles(''tariff_fleet'', ''PATCH /contract/{contractId}/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''tariff_fleet'', ''PATCH /contract/{contractId}/deactivate/'', ''ROLE_ADMIN_DATA_MASTER'', true)';
            EXECUTE 'call migrations.fill_roles(''tariff_fleet'', ''PATCH /contract/{contractId}/deactivate/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''tariff_fleet'', ''GET /contract/{contractId}/'', ''ROLE_ADMIN_DATA_MASTER'', true)';
            EXECUTE 'call migrations.fill_roles(''tariff_fleet'', ''GET /contract/{contractId}/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
        END IF;
    END
$do$;
