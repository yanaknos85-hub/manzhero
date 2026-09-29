CREATE TABLE corporate_approvals.approvals (
    employee_id uuid NOT NULL,
    action_id uuid NOT NULL
);

COMMENT ON TABLE corporate_approvals.approvals IS 'Согласования';
COMMENT ON COLUMN corporate_approvals.approvals.employee_id IS 'Идентификатор согласующего сотрудника';
COMMENT ON COLUMN corporate_approvals.approvals.action_id IS 'Идентификатор согласуемой сущности';