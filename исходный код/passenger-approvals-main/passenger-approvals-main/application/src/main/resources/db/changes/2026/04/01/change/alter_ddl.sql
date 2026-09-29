DROP INDEX IF EXISTS approvals.update_trip_request_approvals_status_idx;
CREATE INDEX IF NOT EXISTS message_employee_department_id_idx ON approvals.message_employee (department_id);

-- Создание ENUM типа
CREATE TYPE approvals.approval_status AS ENUM ('NEW', 'EDITED', 'APPROVED', 'DECLINED', 'CANCELLED');

-- Пример изменения таблицы
ALTER TABLE approvals.final_trip_approvals ALTER COLUMN "status" DROP DEFAULT;
ALTER TABLE approvals.final_trip_approvals ALTER COLUMN "status" TYPE approvals.approval_status USING status::text::approvals.approval_status;
ALTER TABLE approvals.final_trip_approvals ALTER COLUMN "status" SET DEFAULT 'NEW'::approvals.approval_status;

ALTER TABLE approvals.shared_ride_approvals ALTER COLUMN "status" DROP DEFAULT;
ALTER TABLE approvals.shared_ride_approvals ALTER COLUMN "status" TYPE approvals.approval_status USING status::text::approvals.approval_status;
ALTER TABLE approvals.shared_ride_approvals ALTER COLUMN "status" SET DEFAULT 'NEW'::approvals.approval_status;

ALTER TABLE approvals.trip_request_approvals ALTER COLUMN "status" DROP DEFAULT;
ALTER TABLE approvals.trip_request_approvals ALTER COLUMN "status" TYPE approvals.approval_status USING status::text::approvals.approval_status;
ALTER TABLE approvals.trip_request_approvals ALTER COLUMN "status" SET DEFAULT 'NEW'::approvals.approval_status;

ALTER TABLE approvals.update_trip_request_approvals ALTER COLUMN "status" DROP DEFAULT;
ALTER TABLE approvals.update_trip_request_approvals ALTER COLUMN "status" TYPE approvals.approval_status USING status::text::approvals.approval_status;
ALTER TABLE approvals.update_trip_request_approvals ALTER COLUMN "status" SET DEFAULT 'NEW'::approvals.approval_status;

-- ============================================
-- ИНДЕКСЫ ДЛЯ final_trip_approvals
-- ============================================

CREATE INDEX IF NOT EXISTS final_trip_approvals_trip_purpose_id_idx
ON approvals.final_trip_approvals (trip_purpose_id);

-- ============================================
-- ИНДЕКСЫ ДЛЯ shared_ride_approvals
-- ============================================

CREATE INDEX IF NOT EXISTS shared_ride_approvals_trip_purpose_id_idx
ON approvals.shared_ride_approvals (trip_purpose_id);

-- ============================================
-- ИНДЕКСЫ ДЛЯ trip_request_approvals
-- ============================================

CREATE INDEX IF NOT EXISTS trip_request_approvals_trip_purpose_id_idx
ON approvals.trip_request_approvals (trip_purpose_id);

-- ============================================
-- ИНДЕКСЫ ДЛЯ update_trip_request_approvals
-- ============================================

CREATE INDEX IF NOT EXISTS update_trip_request_approvals_trip_purpose_id_idx
ON approvals.update_trip_request_approvals (trip_purpose_id);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'final_trip_approvals_trip_purpose_fk') THEN
        ALTER TABLE approvals.final_trip_approvals ADD CONSTRAINT final_trip_approvals_trip_purpose_fk FOREIGN KEY (trip_purpose_id) REFERENCES approvals.trip_purpose(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'shared_ride_approvals_trip_purpose_fk') THEN
        ALTER TABLE approvals.shared_ride_approvals ADD CONSTRAINT shared_ride_approvals_trip_purpose_fk FOREIGN KEY (trip_purpose_id) REFERENCES approvals.trip_purpose(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'trip_request_approvals_trip_purpose_fk') THEN
        ALTER TABLE approvals.trip_request_approvals ADD CONSTRAINT trip_request_approvals_trip_purpose_fk FOREIGN KEY (trip_purpose_id) REFERENCES approvals.trip_purpose(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'update_trip_request_approvals_trip_purpose_fk') THEN
        ALTER TABLE approvals.update_trip_request_approvals ADD CONSTRAINT update_trip_request_approvals_trip_purpose_fk FOREIGN KEY (trip_purpose_id) REFERENCES approvals.trip_purpose(id);
    END IF;
END $$;