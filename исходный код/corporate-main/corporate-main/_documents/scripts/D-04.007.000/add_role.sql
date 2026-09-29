INSERT INTO authentication.role (code, name, description, data_master, default_for) VALUES ('ROLE_COURIER', 'Курьер', 'Курьер', false, '[]');

INSERT INTO sudir.role (code, name, description, data_master, default_for) VALUES ('ROLE_COURIER', 'Курьер', 'Курьер', false, '[]');

INSERT INTO roles."role"
(code, "name", description, data_master, default_for, exclusive)
VALUES('ROLE_COURIER', 'Курьер', 'Курьер', false, '[]'::jsonb, '["INTERNAL"]'::json);
