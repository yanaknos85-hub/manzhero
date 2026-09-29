-- ============================================
-- Миграция данных из таблицы trip_request_approvals
-- ============================================
INSERT INTO approvals.approval_journal (
    id,
    approval_id,
    actor_id,
    action_id,
    status,
    creation_time,
    approved_by_id,
    transport_type,
    taxi_class,
    desired_date,
    trip_purpose_id,
    expected_cost,
    expected_time,
    expected_distance,
    human_readable_id,
    passenger_count,
    waypoints,
    shared_ride_id,
    add_request_id,
    time_zone,
    type
)
SELECT
    gen_random_uuid(),
    id as approval_id,
    actor_id,
    action_id,
    status,
    creation_time,
    approved_by_id,
    transport_type,
    trip_class,
    desired_date,
    trip_purpose_id,
    expected_cost,
    expected_time,
    expected_distance,
    human_readable_id,
    passenger_count,
    waypoints,
    shared_ride_id,
    NULL as add_request_id,
    time_zone,
    'TRIP_REQUEST'::approvals.approval_journal_type AS type
FROM approvals.trip_request_approvals;