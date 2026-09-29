delete from tariff_fleet.roles vr
where vr.url_id in (select id from tariff_fleet.urls where url = '/contracts/{contractId}/tariffs/' and method = 'POST');
delete from tariff_fleet.urls where url = '/contracts/{contractId}/tariffs/' and method = 'POST';

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
            EXECUTE 'call migrations.fill_roles(''tariff_fleet'', ''GET /tariffs/{id}/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''tariff_fleet'', ''POST /tariffs/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
        END IF;
    END
$do$;