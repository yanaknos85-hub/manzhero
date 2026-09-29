call migrations.fill_roles('corporate', 'POST /executorGroup/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('corporate', 'POST /executorGroup/', 'ROLE_ADMIN_CORP_CLIENT', true);
call migrations.fill_roles('corporate', 'GET /executorGroup/{executorGroupId}/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('corporate', 'GET /executorGroup/{executorGroupId}/', 'ROLE_ADMIN_CORP_CLIENT', true);
call migrations.fill_roles('corporate', 'PUT /executorGroup/{executorGroupId}/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('corporate', 'PUT /executorGroup/{executorGroupId}/', 'ROLE_ADMIN_CORP_CLIENT', true);