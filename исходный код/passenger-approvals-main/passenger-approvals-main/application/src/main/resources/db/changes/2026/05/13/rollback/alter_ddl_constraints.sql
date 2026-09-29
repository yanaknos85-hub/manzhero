ALTER TABLE approvals.approval_journal DROP CONSTRAINT IF EXISTS fk_approval_journal_actor_id;
ALTER TABLE approvals.approval_journal DROP CONSTRAINT IF EXISTS fk_approval_journal_approved_by_id;
ALTER TABLE approvals.approval_journal DROP CONSTRAINT IF EXISTS fk_approval_journal_trip_purpose_id;
ALTER TABLE approvals.approval_journal DROP CONSTRAINT IF EXISTS pk_approval_journal;
ALTER TABLE approvals.approval_journal DROP CONSTRAINT IF EXISTS uk_approval_journal_approval_id;