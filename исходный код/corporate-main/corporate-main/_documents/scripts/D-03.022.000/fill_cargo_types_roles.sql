CALL migrations.fill_roles('corporate','POST /{organizationId}/cargo/type/','ROLE_EMPLOYEE_CORP_CLIENT',true);

CALL migrations.fill_roles('corporate','GET /{organizationId}/cargo/type/search/','ROLE_EMPLOYEE_CORP_CLIENT',true);