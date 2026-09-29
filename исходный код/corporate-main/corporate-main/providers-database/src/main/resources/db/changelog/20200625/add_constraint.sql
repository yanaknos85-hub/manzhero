alter table corporate.delegate
    add constraint uk_delegate_supevisor_date unique (user_id, supervisor_id, start_date);
comment on table corporate.delegate is 'Записи о делегировании';
comment on column corporate.delegate.start_date is 'Дата начала периода делегирования';
comment on column corporate.delegate.end_date is 'Дата окончания периода делегирования';
comment on column corporate.delegate.id is 'Дата начала периода делегирования';
comment on column corporate.delegate.user_id is 'Идентификатор делегата';
comment on column corporate.delegate.supervisor_id is 'Идентификатор руководителя';