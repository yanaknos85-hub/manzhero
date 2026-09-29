alter table tariff_fleet.organization_medical_license
    add issue_date date not null;

alter table tariff_fleet.organization_medical_license
    add expiry_date date not null;

comment on column tariff_fleet.organization_medical_license.issue_date is 'Дата выдачи';
comment on column tariff_fleet.organization_medical_license.expiry_date is 'Дата окончания срока действия';