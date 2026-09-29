package ru.sber.transport.tariff_fleet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.tariff_fleet.constant.InspectionType;
import ru.sber.transport.tariff_fleet.database.dao.EwbContractRepository;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.EwbContract;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.Tariff;
import ru.sber.transport.tariff_fleet.dto.OrganizationMedicalLicensePatchDto;
import ru.sber.transport.tariff_fleet.dto.OrganizationMedicalLicensePostDto;
import ru.sber.transport.tariff_fleet.dto.ewb.*;
import ru.sber.transport.tariff_fleet.exception.*;
import ru.sber.transport.tariff_fleet.mapper.EwbContractMapper;
import ru.sber.transport.tariff_fleet.mapper.EwbTariffMapper;
import ru.sber.transport.tariff_fleet.mapper.OrganizationMedicalLicenseMapper;
import ru.sber.transport.tariff_fleet.messaging.sender.EwbContractSender;
import ru.sber.transport.tariff_fleet.messaging.sender.EwbTariffSender;
import ru.sber.transport.tariff_fleet.messaging.sender.OrganizationMedicalLicenseSender;
import ru.sber.transport.tariff_fleet.service.EdfOperatorService;
import ru.sber.transport.tariff_fleet.service.EwbContractService;
import ru.sber.transport.tariff_fleet.service.OrganizationService;
import ru.sber.transport.tariff_fleet.service.tariff.EwbTariffService;
import ru.sber.transport.tariff_fleet.service.validation.EwbContractValidationService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class EwbContractServiceImpl implements EwbContractService {
    private static final String LICENSE_DATES_VALIDATION_EXCEPTION = "Дата выдачи медицинской лицензии не может быть позже даты окончания действия";

    private final OrganizationService organizationService;
    private final EdfOperatorService edfOperatorService;
    private final EwbContractRepository ewbContractRepository;
    private final EwbContractValidationService ewbContractValidationService;
    private final EwbContractMapper ewbContractMapper;
    private final OrganizationMedicalLicenseMapper organizationMedicalLicenseMapper;
    private final EwbContractSender ewbContractSender;
    private final OrganizationMedicalLicenseSender organizationMedicalLicenseSender;
    private final EwbTariffService ewbTariffService;
    private final EwbTariffSender ewbTariffSender;
    private final EwbTariffMapper ewbTariffMapper;
    private final Clock clock;

    @Override
    @Transactional
    public void create(EwbContractPostDto contractPostDto, Contract contract) {
        validateOrganization(contractPostDto.getContractorOrganizationId());
        validateEdfOperator(contractPostDto.getEdfOperatorId());
        validateOrganizationMedicalLicense(contractPostDto.getInspectionType(), contractPostDto.getContractorMedicalLicense());
        validateUniqueByData(contractPostDto.getContractorOrganizationId(),
                contractPostDto.getInspectionType(),
                contractPostDto.getNumber());
        var ewbContract = ewbContractMapper.ewbContractPostDtoToEwbContract(contractPostDto, contract);
        var saved = ewbContractRepository.save(ewbContract);
        sendToKafka(saved, InspectionType.getMedicineTypes().contains(saved.getInspectionType()));
    }

    @Override
    public Page<EwbContractGetDto> search(EwbSearchContractDto searchContractDto, LocalDate start, LocalDate end) {
        var result = ewbContractRepository.searchEwbContracts(searchContractDto.getContractorOrganizationId(),
                getInspectionType(searchContractDto.getInspectionType()),
                searchContractDto.getNumber(),
                start,
                end,
                searchContractDto.getActive(),
                searchContractDto.getPageRequest());
        var mappedResult = ewbContractMapper.listGetEwbContractProjectionToListEwbContractGetDto(result.getContent());
        return new PageImpl<>(
                mappedResult,
                result.getPageable(),
                result.getTotalElements());
    }

    @Override
    @Transactional
    public void createTariff(EwbTariffPostDto tariffPostDto, Tariff tariff) {
        var contract = ewbContractRepository.findById(tariff.getContractId())
                .orElseThrow(() -> new EwbContractNotFoundException(tariff.getContractId()));
        checkContractIsActive(contract.getContract().isActive(), contract.getContractId());
        ewbTariffService.createTariff(tariffPostDto, tariff, contract.getInspectionType());
    }

    @Override
    @Transactional
    public void edit(UUID id, EwbContractPatchDto contractPatchDto) {
        var ewbContract = getEwbContractWithMedicalLicense(id);
        checkContractIsActive(ewbContract.getContract().isActive(), ewbContract.getContractId());
        checkHaveAnythingToEdit(contractPatchDto, ewbContract.getInspectionType());
        checkAndEditUvhd(contractPatchDto.getUvhd(), ewbContract);
        checkAndEditEdfOperatorId(contractPatchDto.getEdfOperatorId(), ewbContract);
        checkAndEditEdfCode(contractPatchDto.getEdfCode(), ewbContract);
        checkAndEditOrganizationMedicalLicense(contractPatchDto.getContractorMedicalLicense(), ewbContract);
        var saved = ewbContractRepository.save(ewbContract);
        sendToKafka(saved, InspectionType.getMedicineTypes().contains(ewbContract.getInspectionType())
                && contractPatchDto.getContractorMedicalLicense() != null);
    }

    @Override
    @Transactional
    public EwbGetContractByIdDto get(UUID id) {
        var ewbContract = getEwbContractWithMedicalLicense(id);
        var contractorName = organizationService.get(ewbContract.getOrganizationId())
                .map(Organization::getOfficialName)
                .orElseThrow(() -> new ContractorOrganizationNotFoundException(ewbContract.getOrganizationId()));
        return ewbContractMapper.ewbContractToEwbGetContractByIdDto(ewbContract, contractorName);
    }

    @Override
    public EwbContract getEwbContract(UUID id) {
        return ewbContractRepository.findById(id).orElseThrow(() -> new EwbContractNotFoundException(id));
    }

    @Override
    public EwbContract getEwbContractWithParentContract(UUID id) {
        return ewbContractRepository.findByIdWithContract(id).orElseThrow(() -> new EwbContractNotFoundException(id));
    }

    @Override
    @Transactional
    public void autoActivate(Contract contract) {
        var contractId = contract.getId();
        var ewbContract = getEwbContract(contractId).setContract(contract);
        ewbContract.getContract().setActive(true);
        var activatedEwbContract = ewbContractRepository.save(ewbContract);
        var activatedEwbTariffs = ewbTariffService.autoActivateAllByContractId(contractId);
        ewbContractSender.send(ewbContractMapper.ewbContractToEwbContractMessage(activatedEwbContract));
        activatedEwbTariffs.forEach(tariff -> ewbTariffSender.send(ewbTariffMapper.ewbTariffToEwbTariffMessage(tariff)));
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        var ewbContract = getEwbContractWithParentContract(id);
        checkContractIsActive(ewbContract.getContract().isActive(), ewbContract.getContractId());
        var departmentIds = ewbTariffService.findActiveTariffDepartmentIds(id);
        ewbContractValidationService.validateActiveEwbExistence(departmentIds, LocalDate.now(clock).plusDays(1));
        ewbContract.getContract()
                .setActive(false)
                .setEnd(LocalDate.now(clock));
        var saved = ewbContractRepository.save(ewbContract);
        sendToKafka(saved, false);
    }

    @Override
    @Transactional
    public void autoDeactivate(UUID contractId) {
        var ewbContract = getEwbContractWithParentContract(contractId);
        var departmentIds = ewbTariffService.findActiveTariffDepartmentIds(contractId);
        ewbContractValidationService.validateActiveEwbExistence(departmentIds, LocalDate.now(clock));
        ewbContract.getContract().setActive(false);
        var deactivatedContract = ewbContractRepository.save(ewbContract);
        var deactivatedEwbTariffs = ewbTariffService.autoDeactivateAllByContractId(contractId);
        ewbContractSender.send(ewbContractMapper.ewbContractToEwbContractMessage(deactivatedContract));
        deactivatedEwbTariffs.forEach(tariff -> ewbTariffSender.send(ewbTariffMapper.ewbTariffToEwbTariffMessage(tariff)));
    }

    private EwbContract getEwbContractWithMedicalLicense(UUID id) {
        return ewbContractRepository.findByIdWithMedicalLicense(id).orElseThrow(() -> new EwbContractNotFoundException(id));
    }

    private void sendToKafka(EwbContract saved, boolean needSendLicense) {
        if (needSendLicense) {
            organizationMedicalLicenseSender.send(
                    organizationMedicalLicenseMapper.organizationMedicalLicenseToOrganizationMedicalLicenseMessage(
                            saved.getOrganizationMedicalLicense()
                    )
            );
        }
        ewbContractSender.send(ewbContractMapper.ewbContractToEwbContractMessage(saved));
    }

    private void checkAndEditUvhd(String uvhd, EwbContract contract) {
        if (uvhd != null) {
            contract.getContract().setUvhd(uvhd);
        }
    }

    private void checkAndEditEdfOperatorId(String edfOperatorId, EwbContract contract) {
        if (edfOperatorId != null) {
            validateEdfOperator(edfOperatorId);
            contract.setEdfOperatorId(edfOperatorId);
        }
    }

    private void checkAndEditEdfCode(String edfCode, EwbContract contract) {
        if (edfCode != null) {
            contract.setEdfCode(edfCode);
        }
    }

    private void checkAndEditOrganizationMedicalLicense(OrganizationMedicalLicensePatchDto licensePatchDto, EwbContract contract) {
        if (InspectionType.getMedicineTypes().contains(contract.getInspectionType()) && licensePatchDto != null) {
            if (licensePatchDto.series() != null) {
                contract.getOrganizationMedicalLicense().setSeries(licensePatchDto.series());
            }
            if (licensePatchDto.number() != null) {
                contract.getOrganizationMedicalLicense().setNumber(licensePatchDto.number());
            }
            if (licensePatchDto.issueDate() != null && licensePatchDto.expiryDate() != null) {
                checkStartDateBeforeEndDate(licensePatchDto.issueDate(), licensePatchDto.expiryDate());
                contract.getOrganizationMedicalLicense().setIssueDate(licensePatchDto.issueDate());
                contract.getOrganizationMedicalLicense().setExpiryDate(licensePatchDto.expiryDate());
            } else if (licensePatchDto.issueDate() != null) {
                checkStartDateBeforeEndDate(licensePatchDto.issueDate(), contract.getOrganizationMedicalLicense().getExpiryDate());
                contract.getOrganizationMedicalLicense().setIssueDate(licensePatchDto.issueDate());
            } else if (licensePatchDto.expiryDate() != null) {
                checkStartDateBeforeEndDate(contract.getOrganizationMedicalLicense().getIssueDate(), licensePatchDto.expiryDate());
                contract.getOrganizationMedicalLicense().setExpiryDate(licensePatchDto.expiryDate());
            }
        }
    }

    private void checkHaveAnythingToEdit(EwbContractPatchDto contractPatchDto, InspectionType inspectionType) {
        if (contractPatchDto.getUvhd() == null
                && contractPatchDto.getEdfOperatorId() == null
                && contractPatchDto.getEdfCode() == null
                && (InspectionType.getMedicineTypes().contains(inspectionType)
                && contractPatchDto.getContractorMedicalLicense().number() == null
                && contractPatchDto.getContractorMedicalLicense().series() == null
                && contractPatchDto.getContractorMedicalLicense().issueDate() == null
                && contractPatchDto.getContractorMedicalLicense().expiryDate() == null
        )) {
            throw new NothingToEditException();
        }
    }

    private void checkContractIsActive(boolean isActive, UUID contractId) {
        if (!isActive) {
            throw new EwbContractNotActiveException(contractId);
        }
    }

    private static String getInspectionType(InspectionType inspectionType) {
        return inspectionType == null ? null : inspectionType.name();
    }

    private void validateUniqueByData(UUID organizationId, InspectionType inspectionType, String number) {
        if (ewbContractRepository.existsByOrganizationIdAndInspectionTypeAndContract_NumberAndContract_ActiveTrue(organizationId,
                inspectionType,
                number)) {
            throw new EwbContractAlreadyExistsException(number);
        }
    }

    private void validateOrganizationMedicalLicense(InspectionType inspectionType, OrganizationMedicalLicensePostDto organizationMedicalLicense) {
        if (InspectionType.getMedicineTypes().contains(inspectionType)) {
            if (organizationMedicalLicense == null) {
                throw new OrganizationMedicalLicenseEmptyException();
            }
            checkStartDateBeforeEndDate(organizationMedicalLicense.issueDate(), organizationMedicalLicense.expiryDate());
        }
    }

    private void checkStartDateBeforeEndDate(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new DateRangeValidationException(LICENSE_DATES_VALIDATION_EXCEPTION);
        }
    }

    private void validateOrganization(UUID id) {
        var optionalOrganization = organizationService.get(id);
        if (optionalOrganization.isEmpty()) {
            throw new ContractorOrganizationNotFoundException(id);
        } else if (!optionalOrganization.get().isActive()) {
            throw new ContractorOrganizationNotActiveException(id);
        }
    }

    private void validateEdfOperator(String id) {
        var optionalEdfOperator = edfOperatorService.getById(id);
        if (optionalEdfOperator.isEmpty()) {
            throw new EdfOperatorNotFoundException(id);
        } else if (!optionalEdfOperator.get().isActive()) {
            throw new EdfOperatorNotActiveException(id);
        }
    }
}
