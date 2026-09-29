CREATE TABLE IF NOT EXISTS corporate.methods
(
    id          UUID PRIMARY KEY,
    name        VARCHAR NOT NULL UNIQUE,
    description VARCHAR
);

COMMENT ON TABLE corporate.methods IS 'Информация о методах';
COMMENT ON COLUMN corporate.methods.id IS 'ID метода';
COMMENT ON COLUMN corporate.methods.name IS 'Наименование метода';
COMMENT ON COLUMN corporate.methods.description IS 'Описание метода';

CREATE TABLE IF NOT EXISTS corporate.method_roles
(
    id        UUID PRIMARY KEY,
    method_id UUID REFERENCES corporate.methods (id),
    role_code VARCHAR,
    CONSTRAINT method_roles_role_method_uk UNIQUE (method_id, role_code)
);

COMMENT ON TABLE corporate.method_roles IS 'Связи методов и ролей';
COMMENT ON COLUMN corporate.method_roles.id IS 'ID связи';
COMMENT ON COLUMN corporate.method_roles.method_id IS 'ID метода';
COMMENT ON COLUMN corporate.method_roles.role_code IS 'Наименование роли';





