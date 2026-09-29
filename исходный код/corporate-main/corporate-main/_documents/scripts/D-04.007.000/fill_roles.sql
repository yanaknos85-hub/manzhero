CALL migrations.fill_roles('corporate','DELETE /self/courier/','ROLE_EMPLOYEE_CORP_CLIENT',true);
CALL migrations.fill_roles('corporate','DELETE /self/courier/','ROLE_ENGINEER_CORP_CLIENT',true);

CALL migrations.fill_roles('corporate','PUT /self/courier/','ROLE_EMPLOYEE_CORP_CLIENT',true);
CALL migrations.fill_roles('corporate','PUT /self/courier/','ROLE_ENGINEER_CORP_CLIENT',true);