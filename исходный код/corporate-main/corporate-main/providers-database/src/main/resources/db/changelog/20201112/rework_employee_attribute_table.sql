DROP TABLE corporate.employee_attribute;
DROP TABLE corporate.attribute;

CREATE TABLE corporate.attribute
(
    id uuid PRIMARY KEY,
    name varchar(128)
);
COMMENT ON COLUMN corporate.attribute.name is 'Признак сотрудника';

CREATE TABLE corporate.employee_attribute
(
    employee_id uuid CONSTRAINT employee_attribute_employee_kf REFERENCES corporate.employee (id),
    attribute_id uuid CONSTRAINT employee_attribute_attribute_kf REFERENCES corporate.attribute (id),
    PRIMARY KEY (employee_id, attribute_id)
);