create index limit_spending_limit_spending_status_idx_manual
    on limits.limit_spending (limit_spending_status);
create index limit_idx_manual
    on limits."limit" (department_id, year);
create index limit_idx_2_manual
    on limits."limit" (organization_id, employee_id, year);
alter table request.request_for_public
    alter column request_status type varchar(255);
create index request_for_public_idx_manual
    on request."request_for_public" (request_status);
alter table request.request_for_carsharing
    alter column request_status type varchar(255);
create index request_for_carsharing_idx_manual
    on request."request_for_carsharing" (request_status);
alter table request.request_for_public add column finished_time timestamp;

create or replace procedure migrations.fill_gaps(transportType text, statuses text[], fillYear int, fillMonth int, schemaName text, tableName text)
language plpgsql
as
$$
    declare
        stmt text;
        spending  limits.limit_spending%rowtype;
        processed numeric = 0;
    begin
        raise notice '%: % month % of year processing started', transportType, fillMonth, fillYear;
        stmt := 'create temporary table "data" as (select md5(random()::text || clock_timestamp()::text)::uuid as id, rft.author_id, rft.passenger_id as employee_id,rft.creation_time as reservation_time,coalesce(rft.finished_time, rft.creation_time) as spending_time,null as cancel_time,rft.expected_cost as sum_reserved,rft.expected_cost as sum_spent,''SPENT''as limit_spending_status,(select lspp.id from limits."limit" l inner join limits.limit_sharing ls on l.id = ls.limit_id and ls.transport_type = rft.transport_type inner join limits.limit_sharing_per_period lspp on ls.id = lspp.limit_sharing_id and lspp.period =trim(upper(to_char(rft.creation_time, ''Month''))) where l.year = extract(year from rft.creation_time) and l.organization_id = (select organization_id from corporate.employee where id = rft.passenger_id) and ((l.employee_id is not null and l.employee_id = rft.passenger_id) or (l.employee_id is null and lspp.id = (with recursive deps as ( select id,parent_id,(select lspp.id as id from limits."limit" l inner join limits.limit_sharing ls on l.id = ls.limit_id and ls.transport_type = rft.transport_type inner join limits.limit_sharing_per_period lspp on ls.id = lspp.limit_sharing_id and lspp.period =trim(upper(to_char(rft.creation_time, ''Month''))) where department_id = dep.id and year = extract(year from rft.creation_time) order by l.limit_status desc limit 1) as lspp_id from corporate.department dep where id = (select employee.department_id from corporate.employee where id = rft.passenger_id) union select dep.id,dep.parent_id,(select lspp.id as id from limits."limit" l inner join limits.limit_sharing ls on l.id = ls.limit_id and ls.transport_type = rft.transport_type inner join limits.limit_sharing_per_period lspp on ls.id = lspp.limit_sharing_id and lspp.period =trim(upper(to_char(rft.creation_time, ''Month''))) where department_id = dep.id and year = extract(year from rft.creation_time) order by l.limit_status desc limit 1) as lspp_id from corporate.department dep join deps on deps.parent_id = dep.id and deps.lspp_id is null )select deps.lspp_id from deps where lspp_id is not null)) ) order by limit_type desc limit 1) as limit_sharing_per_period_id,rft.id as request_id,(select employee.organization_id from corporate.employee where id = rft.passenger_id) as organization_id from "' || schemaName || '"."'|| tableName ||'" rft where rft.request_status in ('||array_to_string(statuses, ',')||') and creation_time between ''' || fillYear || '-' || fillMonth || '-01T00:00''::timestamp and (''' || fillYear || '-' || fillMonth || '-01T00:00''::timestamp) + interval ''1 month'' and id not in (select lsp.request_id from limits.limit_spending lsp inner join limits.limit_sharing_per_period lspp on lsp.limit_sharing_per_period_id = lspp.id inner join limits.limit_sharing ls on lspp.limit_sharing_id = ls.id where ls.transport_type = ''' || transportType || '''and lsp.limit_spending_status = ''SPENT'') order by reservation_time desc);';
        execute stmt;
        raise notice 'Updating spendings for %', transportType;
        for spending in (select * from "data")
            loop
                processed := processed + 1;
                raise notice '%. Spending % updating', spending.id, processed;
                insert into limits.limit_spending (id, author_id, employee_id, reservation_time, spending_time,
                                                   cancel_time,
                                                   sum_reserved, sum_spent, limit_spending_status,
                                                   limit_sharing_per_period_id,
                                                   request_id, organization_id)
                values (spending.id, spending.author_id, spending.employee_id, spending.reservation_time,
                        spending.spending_time,
                        spending.cancel_time,
                        spending.sum_reserved, spending.sum_spent, spending.limit_spending_status,
                        spending.limit_sharing_per_period_id,
                        spending.request_id, spending.organization_id)
                on conflict(request_id) do update set limit_spending_status = excluded.limit_spending_status,
                                                      sum_spent             = excluded.sum_spent;
                raise notice '%. Spending % updated', spending.id, processed;
            end loop;
        raise notice '%: % month % of year processed', transportType, fillMonth, fillYear;
        drop table "data";
        raise notice 'Temporary table dropped';
    end;
$$;
do
$$
    begin
        for fillYear in 2021..2023 loop
            for fillMonth in 1..12 loop
                    call migrations.fill_gaps('TAXI', '{''TAXI_TRIP_FINISHED''}', fillYear, fillMonth, 'request', 'request_for_taxi');
                    call migrations.fill_gaps('PERSONAL', '{''PERSONAL_TRIP_FINISHED'', ''PERSONAL_PAYMENT_AWAITING'', ''PERSONAL_PAYMENT_DONE'',''PERSONAL_ORDER_PAYMENT_FORMATION''}', fillYear, fillMonth, 'request', 'request_for_personal');
                    call migrations.fill_gaps('PUBLIC', '{''PUBLIC_AWAITING_AFFIRMATIVE'', ''PUBLIC_AFFIRMED'', ''PUBLIC_ORDER_PAYMENT_FORMATION'', ''PUBLIC_PAYMENT_AWAITING'', ''PUBLIC_PAYMENT_DONE''}', fillYear, fillMonth, 'request', 'request_for_public');
                    call migrations.fill_gaps('CARSHARING', '{''CARSHARING_TRIP_FINISHED''}', fillYear, fillMonth, 'request', 'request_for_carsharing');
                end loop;
        end loop;
    end;
$$;
end;

drop procedure migrations.fill_gaps(text, text[], int, int, text, text);

refresh materialized view limits.limit_stats;
refresh materialized view limits.limit_stats_spending;

alter table request.request_for_public drop column finished_time;
drop index limits.limit_spending_limit_spending_status_idx_manual;
drop index limits.limit_idx_manual;
drop index limits.limit_idx_2_manual;
drop index request.request_for_public_idx_manual;
alter table request.request_for_public
    alter column request_status type text;
drop index request.request_for_carsharing_idx_manual;
alter table request.request_for_carsharing
    alter column request_status type text;