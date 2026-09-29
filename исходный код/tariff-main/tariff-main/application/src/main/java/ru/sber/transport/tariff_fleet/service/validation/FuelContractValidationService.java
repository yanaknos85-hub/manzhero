package ru.sber.transport.tariff_fleet.service.validation;

import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.Contractor;
import ru.sber.transport.tariff_fleet.database.model.FuelContract;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointDto;
import ru.sber.transport.tariff_fleet.model.PartiallyUpdateFuelContractModel;

import java.util.List;
import java.util.UUID;

public interface FuelContractValidationService {
    void validateContractorIdAndNumberUnique(UUID organizationId, UUID contractorId, String number, UUID contractId);

    void validateContractor(List<ServicePointDto> servicePointDtoList, Contractor contractor);

    void validateOrganizationPermission(FuelContract fuelContract, UUID organizationId);

    void validateUpdateContract(FuelContract repairContract, PartiallyUpdateFuelContractModel partiallyUpdateFuelContractModel);

    void validateContractActive(Contract contract);

    void validateContractTypeForServicePointUpdating(Contractor contractor);

    void validateDeactivateContract(FuelContract repairContract);
}
