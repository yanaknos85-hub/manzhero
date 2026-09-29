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
            EXECUTE 'call migrations.fill_roles(''tariff_fleet'', ''PATCH /contracts/{contractId}/service-points/all-organizations/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''tariff_fleet'', ''PATCH /contracts/{contractId}/service-points/self-organization/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';

        END IF;
    END
$do$;