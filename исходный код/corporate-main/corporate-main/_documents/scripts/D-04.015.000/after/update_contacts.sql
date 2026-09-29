update authentication.account t
    set email = s.email,
    phone = s.mobile_phone
    from corporate.employee s
    where t.id = s.id;

update notifications_corporate.employee t
set email = s.email,
    phone = s.mobile_phone,
    phone_confirmed = s.phone_confirmed
from corporate.employee s
where t.id = s.id;

update notifications.contacts t
set email = s.email,
    phone = s.mobile_phone,
    phone_confirmed = s.phone_confirmed
from corporate.employee s
where t.id = s.id;