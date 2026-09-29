TRUNCATE TABLE tariff_fleet.repair_contract;
ALTER TABLE tariff_fleet.repair_contract RENAME COLUMN amount TO amount_without_vat;
ALTER TABLE tariff_fleet.repair_contract ADD COLUMN amount_with_vat BIGINT NOT NULL;
ALTER TABLE tariff_fleet.repair_contract ADD COLUMN organization_id UUID NOT NULL
                                                                            CONSTRAINT repair_contract_organization_id_fk
                                                                                REFERENCES tariff_fleet.organization(id);
ALTER TABLE tariff_fleet.repair_contract ADD COLUMN logo_s3_id UUID;