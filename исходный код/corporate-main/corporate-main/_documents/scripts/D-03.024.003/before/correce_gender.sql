update corporate.employee
set gender = 'MALE'
where patronymic is null or patronymic ilike '%вич';

update corporate.employee
set gender = 'FEMALE'
where patronymic ilike '%вна';

update intergation_easup.department
set state = 'IDLE';
update intergation_easup.employee
set state = 'IDLE';
update intergation_easup.position
set state = 'IDLE';
