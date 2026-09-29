insert into approvals.tariff_message (id, humanreadableid, organization_id, region_id, transport_type, contract_id)
(select id, humanreadableid, organization_id, region_id, transport_type, contract_id from tariff.tariff where id not in (select id from approvals.tariff_message) and active is true);

insert into approvals.taxi_tariff_message (id, humanreadableid, region_id, service_type, organization_id, transport_type, contract_id, taxi_class)
(select id, humanreadableid, region_id, service_type, organization_id, transport_type, contract_id, taxi_class from tariff.tariff where id not in (select id from approvals.taxi_tariff_message) and active is true and transport_type = 'TAXI');