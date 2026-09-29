CREATE TABLE corporate.attribute
(
    name varchar(128) PRIMARY KEY
);
COMMENT ON COLUMN corporate.attribute.name is 'Признак сотрудника';

CREATE TABLE corporate.employee_attribute
(
    employee_id uuid CONSTRAINT employee_attribute_employee_kf REFERENCES corporate.employee (id),
    attribute_name  varchar(128) CONSTRAINT employee_attribute_attribute_kf REFERENCES corporate.attribute (name),
    PRIMARY KEY (employee_id, attribute_name)
);