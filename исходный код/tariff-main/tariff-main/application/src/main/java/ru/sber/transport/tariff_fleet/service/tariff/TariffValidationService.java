package ru.sber.transport.tariff_fleet.service.tariff;

import ru.sber.transport.tariff_fleet.database.model.*;

import java.util.Optional;
import java.util.UUID;

public interface TariffValidationService {

    void validateCreateTariff(Contract contract);

    void validateDeactivateTariff(Tariff tariff);

    void validateOrganizationPermission(UUID tariffId, UUID tariffOrganizationId, UUID organizationId);

    void validateCreateRepairTariff(Optional<RepairTariff> existedTariff);

    void validateCreateFuelTariff(Optional<FuelTariff> existedTariff, Department department, Contract contract, Organization fuelContractOrganization);
}
