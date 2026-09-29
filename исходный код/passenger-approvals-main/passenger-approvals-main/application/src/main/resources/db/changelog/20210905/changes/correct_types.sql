alter table approvals.approvals_settings
    alter column min_cost_to_be_approved type int8;
alter table approvals.purpose_and_region_items
    alter column min_cost_to_be_approved type int8;
alter table approvals.messages_geo_zone
    alter column code type int4;