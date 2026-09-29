insert into corporate.messages_roles (code, "name")
    (select code, "name" from authentication.role)
    on conflict do nothing;

insert into corporate.messages_geo_zone (id, "name", parent_id, code)
    (select id, "name", geo_zone.parent_id, code from geo_zones.geo_zone)
    on conflict do nothing;