call migrations.fill_roles('fraud_monitoring', 'GET /{requestId}', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('fraud_monitoring', 'GET /{requestId}', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('fraud_monitoring', 'POST /report', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('fraud_monitoring', 'POST /report', 'ROLE_ENGINEER_CORP_CLIENT', true);
