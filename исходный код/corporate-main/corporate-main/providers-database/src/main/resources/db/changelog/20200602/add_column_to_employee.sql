CREATE TABLE corporate.employee_transport_type
(
    employee_id       uuid CONSTRAINT transport_type_employee_fk REFERENCES corporate.employee (id),
    transport_type_id uuid CONSTRAINT transport_type_type_fk REFERENCES corporate.transport_type (id),
    PRIMARY KEY (employee_id, transport_type_id)
)