package ru.sber.transport.tariff_fleet.service.tariff.impl;

import org.springframework.stereotype.Component;
import ru.sber.transport.tariff_fleet.database.model.*;
import ru.sber.transport.tariff_fleet.exception.*;
import ru.sber.transport.tariff_fleet.service.tariff.TariffValidationService;

import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static ru.sber.transport.tariff_fleet.exception.ContractNotActiveException.CREATE_TARIFF_OF_NOT_ACTIVE_CONTRACT_ERROR_MSG;
import static ru.sber.transport.tariff_fleet.exception.OrganizationPermissionException.TARIFF_PERMISSION_ERROR_MESSAGE;
import static ru.sber.transport.tariff_fleet.exception.TariffNotActiveException.DEACTIVATE_TARIFF_ERROR_MESSAGE;

@Component
public class TariffValidationServiceImpl implements TariffValidationService {

    @Override
    public void validateCreateTariff(Contract contract) {
        if (!contract.isActive()) {
            throw new ContractNotActiveException(CREATE_TARIFF_OF_NOT_ACTIVE_CONTRACT_ERROR_MSG, contract.getId());
        }
    }

    @Override
    public void validateDeactivateTariff(Tariff tariff) {
        if (!tariff.isActive()) {
            throw new TariffNotActiveException(DEACTIVATE_TARIFF_ERROR_MESSAGE, tariff.getId());
        }
    }

    @Override
    public void validateOrganizationPermission(UUID tariffId, UUID tariffOrganizationId, UUID organizationId) {
        if (ofNullable(tariffOrganizationId).map(it -> !organizationId.equals(it)).orElse(false)) {
            throw new OrganizationPermissionException(TARIFF_PERMISSION_ERROR_MESSAGE, tariffId);
        }
    }

    @Override
    public void validateCreateRepairTariff(Optional<RepairTariff> existedActiveTariff) {
        existedActiveTariff.ifPresent(tariff -> {
            throw new TariffAlreadyExistedException(tariff.getTariff().getId(), tariff.getTariff().getContractId());
        });
    }

    @Override
    public void validateCreateFuelTariff(Optional<FuelTariff> existedActiveTariff, Department department, Contract contract,
                                         Organization fuelContractOrganization) {
        existedActiveTariff.ifPresent(tariff -> {
            throw new TariffAlreadyExistedException(tariff.getTariff().getId(), tariff.getTariff().getContractId(), department.getId());
        });

        if (!department.isActive()) {
            throw new DepartmentNotActiveException(department.getId());
        }

        if (!department.getOrganizationId().equals(fuelContractOrganization.getId())) {
            throw new DepartmentIsNotAssignedToContractOrganizationException();
        }
    }
}
