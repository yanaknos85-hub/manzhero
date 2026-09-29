insert into corporate.messages_roles (code, "name")
(select code, "name" from roles."role") on conflict do nothing;