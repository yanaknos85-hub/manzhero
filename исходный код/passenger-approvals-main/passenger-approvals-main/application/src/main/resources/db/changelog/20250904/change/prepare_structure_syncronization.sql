update approvals.message_organization ro
set digit_id = (select co.digit_id from corporate.organization co where co.id = ro.id)
where ro.id in (select id from corporate.organization);

update approvals.message_position ro
set self_approved = (select co.self_approved from corporate.position co where co.id = ro.id)
where ro.id in (select id from corporate.organization);

update approvals.message_department ro
set department_name = (select co.name from corporate.department co where co.id = ro.id)
where ro.id in (select id from corporate.organization);

update approvals.message_employee ro
set first_name = (select co.first_name from corporate.employee co where co.id = ro.id)
where ro.id in (select id from corporate.organization);

update approvals.message_employee ro
set last_name = (select co.last_name from corporate.employee co where co.id = ro.id)
where ro.id in (select id from corporate.organization);

update approvals.message_employee ro
set personnel_number = (select co.personnel_number from corporate.employee co where co.id = ro.id)
where ro.id in (select id from corporate.organization);

update approvals.message_employee ro
set department_id = (select co.department_id from corporate.employee co where co.id = ro.id)
where ro.id in (select id from corporate.organization);

update approvals.message_employee ro
set position_id = (select co.position_id from corporate.employee co where co.id = ro.id)
where ro.id in (select id from corporate.organization);


