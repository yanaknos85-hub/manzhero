delete from tariff_fleet.roles vr where vr.url_id in (select id from tariff_fleet.urls where url in (
                                         '/contractors/{documentType}/all-organizations/',
                                         '/contractors/{documentType}/self-organization/',
                                         '/contracts/{contractId}/all-organizations/',
                                         '/contracts/{contractId}/self-organization/',
                                         '/contracts/{contractId}/all-organizations/',
                                         '/contracts/{contractId}/self-organization/'));
delete from tariff_fleet.urls where url in (
                                         '/contractors/{documentType}/all-organizations/',
                                         '/contractors/{documentType}/self-organization/',
                                         '/contracts/{contractId}/all-organizations/',
                                         '/contracts/{contractId}/self-organization/',
                                         '/contracts/{contractId}/all-organizations/',
                                         '/contracts/{contractId}/self-organization/');

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
            EXECUTE 'call migrations.fill_roles(''tariff_fleet'', ''GET /contractors/{documentType}/all-organizations/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''tariff_fleet'', ''GET /contractors/{documentType}/self-organization/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''tariff_fleet'', ''PATCH /contracts/{contractId}/all-organizations/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''tariff_fleet'', ''PATCH /contracts/{contractId}/self-organization/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''tariff_fleet'', ''GET /contracts/{contractId}/all-organizations/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''tariff_fleet'', ''GET /contracts/{contractId}/self-organization/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';

        END IF;
    END
$do$;