ALTER TABLE corporate_approvals.approvals
    ADD CONSTRAINT employee_approvals_fk FOREIGN KEY (employee_id) REFERENCES corporate.employee (id);