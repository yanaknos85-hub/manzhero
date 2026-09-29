DROP INDEX IF EXISTS approvals.message_employee_department_id_idx;

DROP INDEX IF EXISTS approvals.final_trip_approvals_trip_purpose_id_idx;

DROP INDEX IF EXISTS approvals.shared_ride_approvals_trip_purpose_id_idx;

DROP INDEX IF EXISTS approvals.trip_request_approvals_trip_purpose_id_idx;

DROP INDEX IF EXISTS approvals.update_trip_request_approvals_trip_purpose_id_idx;

ALTER TABLE approvals.final_trip_approvals DROP CONSTRAINT IF EXISTS final_trip_approvals_trip_purpose_fk;
ALTER TABLE approvals.shared_ride_approvals DROP CONSTRAINT IF EXISTS shared_ride_approvals_trip_purpose_fk;
ALTER TABLE approvals.trip_request_approvals DROP CONSTRAINT IF EXISTS trip_request_approvals_trip_purpose_fk;
ALTER TABLE approvals.update_trip_request_approvals DROP CONSTRAINT IF EXISTS update_trip_request_approvals_trip_purpose_fk;

ALTER TABLE approvals.final_trip_approvals ALTER COLUMN "status" DROP DEFAULT;
ALTER TABLE approvals.final_trip_approvals ALTER COLUMN "status" TYPE text;
ALTER TABLE approvals.final_trip_approvals ALTER COLUMN "status" SET DEFAULT 'NEW';

ALTER TABLE approvals.shared_ride_approvals ALTER COLUMN "status" DROP DEFAULT;
ALTER TABLE approvals.shared_ride_approvals ALTER COLUMN "status" TYPE text;
ALTER TABLE approvals.shared_ride_approvals ALTER COLUMN "status" SET DEFAULT 'NEW';

ALTER TABLE approvals.trip_request_approvals ALTER COLUMN "status" DROP DEFAULT;
ALTER TABLE approvals.trip_request_approvals ALTER COLUMN "status" TYPE text;
ALTER TABLE approvals.trip_request_approvals ALTER COLUMN "status" SET DEFAULT 'NEW';

ALTER TABLE approvals.update_trip_request_approvals ALTER COLUMN "status" DROP DEFAULT;
ALTER TABLE approvals.update_trip_request_approvals ALTER COLUMN "status" TYPE text;
ALTER TABLE approvals.update_trip_request_approvals ALTER COLUMN "status" SET DEFAULT 'NEW';

DROP TYPE IF EXISTS approvals.approval_status;