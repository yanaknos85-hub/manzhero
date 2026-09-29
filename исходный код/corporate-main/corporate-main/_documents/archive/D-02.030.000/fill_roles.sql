call migrations.fill_roles('corporate', 'POST /groups', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('corporate', 'GET /groups', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('corporate', 'PATCH /groups/{organizationGroupId}', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('corporate', 'PUT /groups/{organizationGroupId}', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('corporate', 'DELETE /groups/{organizationGroupId}', 'ROLE_ADMIN_DATA_MASTER', true);