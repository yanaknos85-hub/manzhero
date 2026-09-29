CALL migrations.fill_roles('corporate','GET /cargo/type/categories/','ROLE_ADMIN_DATA_MASTER',true);
CALL migrations.fill_roles('corporate','GET /cargo/type/categories/','ROLE_ENGINEER_CORP_CLIENT',true);

CALL migrations.fill_roles('corporate','GET /cargo/type/types/','ROLE_ADMIN_DATA_MASTER',true);
CALL migrations.fill_roles('corporate','GET /cargo/type/types/','ROLE_ENGINEER_CORP_CLIENT',true);

CALL migrations.fill_roles('corporate','GET /{organizationId}/cargo/type/','ROLE_ADMIN_DATA_MASTER',true);
CALL migrations.fill_roles('corporate','GET /{organizationId}/cargo/type/','ROLE_ENGINEER_CORP_CLIENT',true);

CALL migrations.fill_roles('corporate','POST /{organizationId}/cargo/type/','ROLE_ADMIN_DATA_MASTER',true);
CALL migrations.fill_roles('corporate','POST /{organizationId}/cargo/type/','ROLE_ENGINEER_CORP_CLIENT',true);

CALL migrations.fill_roles('corporate','GET /{organizationId}/cargo/type/search/','ROLE_ADMIN_DATA_MASTER',true);
CALL migrations.fill_roles('corporate','GET /{organizationId}/cargo/type/search/','ROLE_ENGINEER_CORP_CLIENT',true);

CALL migrations.fill_roles('corporate','GET /{organizationId}/cargo/type/{typeId}/','ROLE_ADMIN_DATA_MASTER',true);
CALL migrations.fill_roles('corporate','GET /{organizationId}/cargo/type/{typeId}/','ROLE_ENGINEER_CORP_CLIENT',true);

CALL migrations.fill_roles('corporate','POST /{organizationId}/cargo/type/{typeId}/','ROLE_ADMIN_DATA_MASTER',true);
CALL migrations.fill_roles('corporate','POST /{organizationId}/cargo/type/{typeId}/','ROLE_ENGINEER_CORP_CLIENT',true);

CALL migrations.fill_roles('corporate','DELETE /{organizationId}/cargo/type/{typeId}/','ROLE_ADMIN_DATA_MASTER',true);
CALL migrations.fill_roles('corporate','DELETE /{organizationId}/cargo/type/{typeId}/','ROLE_ENGINEER_CORP_CLIENT',true);