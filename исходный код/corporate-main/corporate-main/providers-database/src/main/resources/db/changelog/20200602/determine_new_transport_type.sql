INSERT INTO corporate.employee_transport_type (employee_id, transport_type_id)
SELECT employee_id, transport_type.id
FROM corporate.employee_transport_types
         INNER JOIN corporate.transport_type ON lower(employee_transport_types.transport_type) = transport_type.name;