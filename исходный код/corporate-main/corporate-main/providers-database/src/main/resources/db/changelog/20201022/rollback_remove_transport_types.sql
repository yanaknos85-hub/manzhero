alter table corporate.employee_transport_type
    add column transport_type_id uuid;

update corporate.employee_transport_type
set transport_type_id = '7f18ce71-99a7-47b5-b285-335058c6715c'
where transport_type='TAXI';
update corporate.employee_transport_type
set transport_type_id = '1a33601d-4db4-4720-8d09-95f015770fe0'
where transport_type='PERSONAL';
update corporate.employee_transport_type
set transport_type_id = 'fa96da51-068d-4cf5-bfd2-4cd28d717e13'
where transport_type='PUBLIC';
update corporate.employee_transport_type
set transport_type_id = '7f18ce71-65a7-47b5-b285-335058c6515c'
where transport_type='CARSHARING';
update corporate.employee_transport_type
set transport_type_id = '6f18ce82-99a7-34b5-b285-335058c6715c'
where transport_type='BICYCLE';
update corporate.employee_transport_type
set transport_type_id = '1f13ce82-99a7-34b5-b285-225058b6715c'
where transport_type='WALK';
update corporate.employee_transport_type
set transport_type_id = '3c13ce82-24a7-11b5-b195-225058b6715c'
where transport_type='SCOOTER';

alter table corporate.employee_transport_type
    drop column transport_type;

alter table corporate.delegate
    add column transport_type_id uuid;

update corporate.delegate
set transport_type_id = '7f18ce71-99a7-47b5-b285-335058c6715c'
where transport_type='TAXI';
update corporate.delegate
set transport_type_id = '1a33601d-4db4-4720-8d09-95f015770fe0'
where transport_type='PERSONAL';
update corporate.delegate
set transport_type_id = 'fa96da51-068d-4cf5-bfd2-4cd28d717e13'
where transport_type='PUBLIC';
update corporate.delegate
set transport_type_id = '7f18ce71-65a7-47b5-b285-335058c6515c'
where transport_type='CARSHARING';
update corporate.delegate
set transport_type_id = '6f18ce82-99a7-34b5-b285-335058c6715c'
where transport_type='BICYCLE';
update corporate.delegate
set transport_type_id = '1f13ce82-99a7-34b5-b285-225058b6715c'
where transport_type='WALK';
update corporate.delegate
set transport_type_id = '3c13ce82-24a7-11b5-b195-225058b6715c'
where transport_type='SCOOTER';
alter table corporate.delegate
    drop column transport_type cascade;

alter table corporate.employee_transport_type
    drop constraint if exists employee_transport_type_pkey cascade;
alter table corporate.employee_transport_type
    add primary key (employee_id, transport_type_id);

alter table corporate.delegate
    drop constraint if exists transport_type_type_id_fk cascade;
alter table corporate.delegate
    drop constraint if exists transport_type_type_fk cascade;

