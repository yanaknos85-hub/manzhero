alter table approvals.trip_request_approvals
    drop column shared_ride_id;
alter table approvals.trip_request_approvals
    add column shared_ride_id UUID;
