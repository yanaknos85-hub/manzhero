create type tariff_fleet.contractor_type as enum (
    'API',
    'DISPATCHER_INTERNAL',
    'DISPATCHER_EXTERNAL',
    'AUTOSERVICE_INTERNAL',
    'AUTOSERVICE_EXTERNAL'
);
create type tariff_fleet.service_type as enum (
    'AUTOSERVICE',
    'EMPLOYEE_TRANSPORTATION',
    'CARGO_TRANSPORTATION'
);