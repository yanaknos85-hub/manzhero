alter table corporate.delegate drop constraint if exists uk_delegate_supevisor_date;
alter table corporate.delegate
    add constraint uk_delegate unique (user_id, supervisor_id, transport_type_id, start_date);
comment on column corporate.delegate.transport_type_id is 'Идентификатор типа транспора';