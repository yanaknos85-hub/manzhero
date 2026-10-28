update roles.role
set exclusive = '["EXTERNAL"]'::json
where code = 'ROLE_EXTERNAL_CLIENT';

delete from sudir.account_roles
where role_code = 'ROLE_EXTERNAL_CLIENT';

delete from sudir.role
where code = 'ROLE_EXTERNAL_CLIENT';