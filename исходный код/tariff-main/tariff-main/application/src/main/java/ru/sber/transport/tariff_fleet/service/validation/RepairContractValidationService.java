package ru.sber.transport.tariff_fleet.service.validation;

import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.Contractor;
import ru.sber.transport.tariff_fleet.database.model.RepairContract;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointDto;
import ru.sber.transport.tariff_fleet.model.PartiallyUpdateRepairContractModel;

import java.util.List;
import java.util.UUID;

public interface RepairContractValidationService {

    void validateOrganizationPermission(RepairContract repairContract, UUID organizationId);

    void validateContractor(List<ServicePointDto> servicePointDtoList, Contractor contractor);

    void validateContractorIdAndNumberUnique(UUID organizationId, UUID contractorId, String number, UUID contractId);

    void validateUpdateContract(RepairContract repairContract, PartiallyUpdateRepairContractModel partiallyUpdateRepairContractModel);

    void validateDeactivateContract(RepairContract repairContract);

    void validateContractActive(Contract contract);

    void validateContractTypeForServicePointUpdating(Contractor contractor);
}
