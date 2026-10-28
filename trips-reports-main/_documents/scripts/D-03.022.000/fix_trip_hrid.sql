alter table trips_reports.report drop constraint trips_reports_report_trip_human_readable_id_uc;
update trips_reports.report set trip_human_readable_id = correct_hrid_select.correct_hrid
from (
    select
    tr.id as trip_id,
    tr.trip_human_readable_id as trip_hrid,
    concat_ws('-', 'TP', lpad(tc.digit_id::text, 4, '0') , lpad(tt.digit_id::text, 8, '0')) as correct_hrid
    from trips_reports.report as tr
    join trips.trips as tt on tt.id = tr.id
    join trips.contractors as tc on tc.id = tt.contractor_id
) as correct_hrid_select
where correct_hrid_select.trip_id = trips_reports.report.id;
alter table trips_reports.report add constraint trips_reports_report_trip_human_readable_id_uc unique (trip_human_readable_id);
delete from trips_reports.report where id not in (select id from trips.trips)