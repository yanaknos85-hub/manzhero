package ru.sber.transport.tariff_fleet.service.validation.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.sber.transport.tariff_fleet.constant.ContractorType;
import ru.sber.transport.tariff_fleet.constant.ServiceType;
import ru.sber.transport.tariff_fleet.database.dao.FuelContractRepository;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.Contractor;
import ru.sber.transport.tariff_fleet.database.model.FuelContract;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointDto;
import ru.sber.transport.tariff_fleet.exception.*;
import ru.sber.transport.tariff_fleet.model.PartiallyUpdateFuelContractModel;
import ru.sber.transport.tariff_fleet.service.tariff.TariffService;
import ru.sber.transport.tariff_fleet.service.validation.FuelContractValidationService;
import ru.sber.transport.tariff_fleet.service.validation.ServicePointValidationService;

import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static ru.sber.transport.tariff_fleet.exception.ContractNotActiveException.*;
import static ru.sber.transport.tariff_fleet.exception.OrganizationPermissionException.CONTRACT_PERMISSION_ERROR_MESSAGE;

@Service
@RequiredArgsConstructor
public class FuelContractValidationServiceImpl implements FuelContractValidationService {
    private final FuelContractRepository fuelContractRepository;
    private final TariffService tariffService;
    private final ServicePointValidationService servicePointValidationService;

    @Override
    public void validateContractorIdAndNumberUnique(UUID organizationId, UUID contractorId, String number, UUID contractId) {
        if (fuelContractRepository.existsByOrganizationIdAndContractorIdAndContract_NumberAndContract_ActiveIsTrue(organizationId, contractorId,
                number)) {
            throw new ContractAlreadyExistsException(contractId);
        }
    }

    @Override
    public void validateContractor(List<ServicePointDto> servicePointDtoList, Contractor contractor) {
        if (!contractor.isActive()) {
            throw new ContractorNotActiveException(contractor.getId());
        }
        if (!ServiceType.AUTOSERVICE.equals(contractor.getServiceType())) {
            throw new ContractorServiceTypeMismatchException(contractor.getId(), contractor.getServiceType(), ServiceType.AUTOSERVICE);
        }

        if (!ContractorType.API.equals(contractor.getContractorType()) && CollectionUtils.isEmpty(servicePointDtoList)) {
            throw new ServicePointsAbsentException();
        }
    }

    @Override
    public void validateOrganizationPermission(FuelContract fuelContract, UUID organizationId) {
        if (ofNullable(fuelContract.getOrganization()).map(it -> !organizationId.equals(it.getId())).orElse(false)) {
            throw new OrganizationPermissionException(CONTRACT_PERMISSION_ERROR_MESSAGE, fuelContract.getContractId());
        }
    }

    @Override
    public void validateUpdateContract(FuelContract fuelContract, PartiallyUpdateFuelContractModel partiallyUpdateFuelContractModel) {
        validateContractActive(fuelContract.getContract());
        partiallyUpdateFuelContractModel.servicePointsOpt().ifPresent(servicePoints -> {
            if (ContractorType.API.equals(fuelContract.getContractor().getContractorType())) {
                throw new WrongContractTypeForServicePointsUpdatingException();
            }
            servicePointValidationService.validateServicePoint(servicePoints);
        });
    }

    @Override
    public void validateContractActive(Contract contract) {
        if (!contract.isActive()) {
            throw new ContractNotActiveException(EDIT_NOT_ACTIVE_CONTRACT_ERROR_MSG, contract.getId());
        }
    }

    @Override
    public void validateContractTypeForServicePointUpdating(Contractor contractor) {
        if (ContractorType.API.equals(contractor.getContractorType())) {
            throw new WrongContractTypeForServicePointsUpdatingException();
        }
    }

    @Override
    public void validateDeactivateContract(FuelContract fuelContract) {
        if (!fuelContract.getContract().isActive()) {
            throw new ContractNotActiveException(DEACTIVATE_NOT_ACTIVE_ERROR_MSG, fuelContract.getContractId());
        }

        var hasActiveTariffs = tariffService.getAllByContractId(fuelContract.getContractId()).stream()
                .anyMatch(tariff -> Boolean.TRUE.equals(tariff.isActive()));

        if (hasActiveTariffs) {
            throw new ContractNotActiveException(DEACTIVATE_CONTRACT_WITH_ACTIVE_TARIFF_ERROR_MSG);
        }
    }
}
