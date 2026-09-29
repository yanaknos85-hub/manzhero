alter table corporate.employee
    alter column changed_by type uuid using changed_by::uuid