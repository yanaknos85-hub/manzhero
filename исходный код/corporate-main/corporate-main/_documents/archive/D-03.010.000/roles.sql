call migrations.fill_roles('corporate', 'POST /files/position/', 'ROLE_ADMIN_CORP_CLIENT', true);
call migrations.fill_roles('corporate', 'POST /files/position/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('corporate', 'POST /files/position/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('corporate', 'GET /files/position/result/{fileName}/', 'ROLE_ADMIN_CORP_CLIENT', true);
call migrations.fill_roles('corporate', 'GET /files/position/result/{fileName}/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('corporate', 'GET /files/position/result/{fileName}/', 'ROLE_ENGINEER_CORP_CLIENT', true);