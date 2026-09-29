package ru.sber.transport.tariff_fleet.service.validation.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.sber.transport.tariff_fleet.constant.ContractorType;
import ru.sber.transport.tariff_fleet.constant.ServiceType;
import ru.sber.transport.tariff_fleet.database.dao.RepairContractRepository;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.Contractor;
import ru.sber.transport.tariff_fleet.database.model.RepairContract;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointDto;
import ru.sber.transport.tariff_fleet.exception.*;
import ru.sber.transport.tariff_fleet.model.PartiallyUpdateRepairContractModel;
import ru.sber.transport.tariff_fleet.service.validation.RepairContractValidationService;
import ru.sber.transport.tariff_fleet.service.validation.ServicePointValidationService;

import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static ru.sber.transport.tariff_fleet.exception.ContractNotActiveException.DEACTIVATE_NOT_ACTIVE_ERROR_MSG;
import static ru.sber.transport.tariff_fleet.exception.ContractNotActiveException.EDIT_NOT_ACTIVE_CONTRACT_ERROR_MSG;
import static ru.sber.transport.tariff_fleet.exception.OrganizationPermissionException.CONTRACT_PERMISSION_ERROR_MESSAGE;

@Service
@RequiredArgsConstructor
public class RepairContractValidationServiceImpl implements RepairContractValidationService {
    private final RepairContractRepository repairContractRepository;
    private final ServicePointValidationService servicePointValidationService;

    @Override
    public void validateOrganizationPermission(RepairContract repairContract, UUID organizationId) {
        if (ofNullable(repairContract.getOrganization()).map(it -> !organizationId.equals(it.getId())).orElse(false)) {
            throw new OrganizationPermissionException(CONTRACT_PERMISSION_ERROR_MESSAGE, repairContract.getContractId());
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
        if (!List.of(ContractorType.API, ContractorType.AUTOSERVICE_EXTERNAL).contains(contractor.getContractorType()) && CollectionUtils.isEmpty(servicePointDtoList)) {
            throw new ServicePointsAbsentException();
        }
    }

    @Override
    public void validateContractorIdAndNumberUnique(UUID organizationId, UUID contractorId, String number, UUID contractId) {
        if (repairContractRepository.existsByOrganizationIdAndContractorIdAndContract_NumberAndContract_ActiveIsTrue(organizationId, contractorId,
                number)) {
            throw new ContractAlreadyExistsException(contractId);
        }
    }

    @Override
    public void validateUpdateContract(RepairContract repairContract, PartiallyUpdateRepairContractModel partiallyUpdateRepairContractModel) {
        validateContractActive(repairContract.getContract());
        partiallyUpdateRepairContractModel.servicePointsOpt().ifPresent(servicePoints -> {
            if (ContractorType.API.equals(repairContract.getContractor().getContractorType())) {
                throw new WrongContractTypeForServicePointsUpdatingException();
            }
            servicePointValidationService.validateServicePoint(servicePoints);
        });
    }

    @Override
    public void validateDeactivateContract(RepairContract repairContract) {
        if (!repairContract.getContract().isActive()) {
            throw new ContractNotActiveException(DEACTIVATE_NOT_ACTIVE_ERROR_MSG, repairContract.getContractId());
        }
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


}
