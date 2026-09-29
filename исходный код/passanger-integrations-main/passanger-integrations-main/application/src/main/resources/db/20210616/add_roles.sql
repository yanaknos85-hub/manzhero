CREATE TABLE IF NOT EXISTS integrations.methods
(
    id          UUID PRIMARY KEY,
    name        VARCHAR NOT NULL UNIQUE,
    description VARCHAR
);

COMMENT ON TABLE integrations.methods IS 'Информация о методах';
COMMENT ON COLUMN integrations.methods.id IS 'ID метода';
COMMENT ON COLUMN integrations.methods.name IS 'Наименование метода';
COMMENT ON COLUMN integrations.methods.description IS 'Описание метода';

CREATE TABLE IF NOT EXISTS integrations.method_roles
(
    id        UUID PRIMARY KEY,
    method_id UUID REFERENCES integrations.methods (id),
    role_code VARCHAR,
    CONSTRAINT method_roles_role_method_uk UNIQUE (method_id, role_code)
);

COMMENT ON TABLE integrations.method_roles IS 'Связи методов и ролей';
COMMENT ON COLUMN integrations.method_roles.id IS 'ID связи';
COMMENT ON COLUMN integrations.method_roles.method_id IS 'ID метода';
COMMENT ON COLUMN integrations.method_roles.role_code IS 'Наименование роли';





