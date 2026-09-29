call migrations.fill_roles('corporate', 'GET /executorGroup/affiliation/{employeeId}/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('corporate', 'GET /executorGroup/affiliation/{employeeId}/', 'ROLE_ADMIN_CORP_CLIENT', true);
call migrations.fill_roles('corporate', 'GET /executorGroup/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('corporate', 'GET /executorGroup/', 'ROLE_ADMIN_CORP_CLIENT', true);
call migrations.fill_roles('corporate', 'GET /employees/search_eg/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('corporate', 'GET /employees/search_eg/', 'ROLE_ADMIN_CORP_CLIENT', true);