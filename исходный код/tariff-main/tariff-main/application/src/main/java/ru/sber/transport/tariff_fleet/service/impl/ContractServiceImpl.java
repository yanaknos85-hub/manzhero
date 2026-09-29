package ru.sber.transport.tariff_fleet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.dao.ContractRepository;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.dto.*;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbContractPatchDto;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbContractPostDto;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbSearchContractDto;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbTariffPostDto;
import ru.sber.transport.tariff_fleet.dto.fuel.*;
import ru.sber.transport.tariff_fleet.dto.repair.*;
import ru.sber.transport.tariff_fleet.dto.service_point.UploadServicePointsDto;
import ru.sber.transport.tariff_fleet.exception.ContractNotActiveException;
import ru.sber.transport.tariff_fleet.exception.ContractNotFoundException;
import ru.sber.transport.tariff_fleet.exception.UnexpectedDocumentTypeValidationException;
import ru.sber.transport.tariff_fleet.exception.UnsupportedDocumentTypeException;
import ru.sber.transport.tariff_fleet.mapper.ContractMapper;
import ru.sber.transport.tariff_fleet.mapper.FuelContractMapper;
import ru.sber.transport.tariff_fleet.mapper.RepairContractMapper;
import ru.sber.transport.tariff_fleet.service.*;
import ru.sber.transport.tariff_fleet.service.tariff.TariffService;
import ru.sber.transport.tariff_fleet.service.validation.ContractValidationService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static ru.sber.transport.tariff_fleet.constant.DocumentType.*;
import static ru.sber.transport.tariff_fleet.exception.ContractNotActiveException.EDIT_TARIFF_OF_NOT_ACTIVE_CONTRACT_ERROR_MSG;
import static ru.sber.transport.tariff_fleet.exception.UnexpectedDocumentTypeValidationException.CONTRACT_TYPE_MSG_FORMAT;


@RequiredArgsConstructor
@Service
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final ContractMapper contractMapper;
    private final EwbContractService ewbContractService;
    private final RepairContractService repairContractService;
    private final RepairContractMapper repairContractMapper;
    private final FuelContractMapper fuelContractMapper;
    private final FuelContractService fuelContractService;
    private final ContractValidationService contractValidationService;
    private final EmployeeService employeeService;
    private final TariffService tariffService;
    private final ContractGetInternalService contractGetInternalService;
    private final Clock clock;

    @Override
    @Transactional
    public void create(AbstractContractPostDto contractPostDto) {
        contractValidationService.validateUvhdUnique(contractPostDto.getUvhd());
        contractValidationService.validateDateRange(contractPostDto.getPeriod().start(), contractPostDto.getPeriod().end());
        var contract = contractMapper.abstractPostContractDtoToContract(contractPostDto,
                isContractActive(contractPostDto.getPeriod().start(),
                        contractPostDto.getPeriod().end()));
        contractRepository.save(contract);
        if (contractPostDto.getDocumentType().equals(EWB)) {
            ewbContractService.create((EwbContractPostDto) contractPostDto, contract);
        } else {
            throw new UnsupportedDocumentTypeException(contractPostDto.getDocumentType(), List.of(EWB));
        }
    }

    @Override
    @Transactional
    public void createAllOrganizations(AbstractContractPostAllOrganizationsDto abstractContractPostAllOrganizationsDto) {
        contractValidationService.validateDateRange(abstractContractPostAllOrganizationsDto.getStart(),
                abstractContractPostAllOrganizationsDto.getEnd());
        var contract = contractMapper.abstractContractPostAllOrganizationsDtoToContract(
                abstractContractPostAllOrganizationsDto,
                isContractActive(abstractContractPostAllOrganizationsDto.getStart(),
                        abstractContractPostAllOrganizationsDto.getEnd()));
        contractRepository.save(contract);
        switch (abstractContractPostAllOrganizationsDto.getDocumentType()) {
            case REPAIR_AND_MAINTENANCE -> repairContractService.createAllOrganizations(
                    (RepairContractPostAllOrganizationsDto) abstractContractPostAllOrganizationsDto,
                    contract);
            case FUEL -> fuelContractService.createAllOrganizations(
                    (FuelContractPostAllOrganizationsDto) abstractContractPostAllOrganizationsDto,
                    contract);
            default -> throw new UnsupportedDocumentTypeException(
                    abstractContractPostAllOrganizationsDto.getDocumentType(),
                    List.of(REPAIR_AND_MAINTENANCE, FUEL));
        }
    }

    @Override
    @Transactional
    public void createSelfOrganization(AbstractContractPostSelfOrganizationDto abstractContractPostSelfOrganizationDto, UUID userId) {
        var employee = employeeService.getByUserId(userId);
        contractValidationService.validateDateRange(abstractContractPostSelfOrganizationDto.getStart(),
                abstractContractPostSelfOrganizationDto.getEnd());
        var contract = contractMapper.abstractContractPostSelfOrganizationDtoToContract(
                abstractContractPostSelfOrganizationDto,
                isContractActive(abstractContractPostSelfOrganizationDto.getStart(),
                        abstractContractPostSelfOrganizationDto.getEnd()));
        contractRepository.save(contract);

        switch (abstractContractPostSelfOrganizationDto.getDocumentType()) {
            case REPAIR_AND_MAINTENANCE -> repairContractService.createSelfOrganization(
                    (RepairContractPostSelfOrganizationDto) abstractContractPostSelfOrganizationDto,
                    contract, employee.getOrganization().getId());
            case FUEL -> fuelContractService.createSelfOrganization(
                    (FuelContractPostSelfOrganizationDto) abstractContractPostSelfOrganizationDto,
                    contract, employee.getOrganization().getId());
            default ->
                    throw new UnsupportedDocumentTypeException(abstractContractPostSelfOrganizationDto.getDocumentType(), List.of(
                            REPAIR_AND_MAINTENANCE, FUEL));
        }
    }

    @Override
    public Page<? extends AbstractContractGetDto> search(AbstractSearchContractDto searchContractDto) {
        var start = contractValidationService.validateStartAndGet(searchContractDto.getPeriod(), searchContractDto.getActive());
        var end = contractValidationService.validateEndAndGet(searchContractDto.getPeriod(), searchContractDto.getActive());
        if (Objects.nonNull(start) && Objects.nonNull(end)) {
            contractValidationService.validateDateRange(start, end);
        }

        switch (searchContractDto.getDocumentType()) {
            case EWB -> {
                return ewbContractService.search((EwbSearchContractDto) searchContractDto, start, end);
            }
            case REPAIR_AND_MAINTENANCE -> {
                return repairContractService.search((RepairSearchContractDto) searchContractDto, start, end);
            }
            default ->
                    throw new UnsupportedDocumentTypeException(searchContractDto.getDocumentType(), List.of(EWB, FUEL));
        }
    }

    @Override
    public Page<? extends AbstractContractGetAllOrganizationsDto> searchAllOrganizations(
            AbstractSearchContractAllOrganizationsDto searchContractDto) {
        contractValidationService.validateDateRange(searchContractDto.getStart(), searchContractDto.getEnd());
        switch (searchContractDto.getDocumentType()) {
            case REPAIR_AND_MAINTENANCE -> {
                return repairContractService.searchAllOrganizations(
                        (RepairSearchContractAllOrganizationsDto) searchContractDto);
            }
            case FUEL -> {
                return fuelContractService.searchAllOrganizations(
                        (FuelSearchContractAllOrganizationsDto) searchContractDto);
            }
            default -> throw new UnsupportedDocumentTypeException(
                    searchContractDto.getDocumentType(),
                    List.of(REPAIR_AND_MAINTENANCE, FUEL));
        }
    }

    @Override
    public Page<? extends AbstractContractGetSelfOrganizationDto> searchSelfOrganization(
            AbstractSearchContractSelfOrganizationDto searchContractDto, UUID userId) {
        contractValidationService.validateDateRange(searchContractDto.getStart(), searchContractDto.getEnd());
        var employee = employeeService.getByUserId(userId);
        switch (searchContractDto.getDocumentType()) {
            case REPAIR_AND_MAINTENANCE -> {
                return repairContractService.searchSelfOrganization(
                        (RepairSearchContractSelfOrganizationDto) searchContractDto,
                        employee.getOrganization().getId());
            }
            case FUEL -> {
                return fuelContractService.searchSelfOrganization(
                        (FuelSearchContractSelfOrganizationDto) searchContractDto,
                        employee.getOrganization().getId());
            }
            default -> throw new UnsupportedDocumentTypeException(searchContractDto.getDocumentType(),
                    List.of(REPAIR_AND_MAINTENANCE, FUEL));
        }
    }

    @Override
    public void edit(UUID contractId, AbstractContractPatchDto contractPatchDto) {
        if (Optional.ofNullable(contractPatchDto.getUvhd()).isPresent()) {
            contractValidationService.validateUvhdUnique(contractPatchDto.getUvhd());
        }
        if (contractPatchDto.getDocumentType().equals(EWB)) {
            ewbContractService.edit(contractId, (EwbContractPatchDto) contractPatchDto);
        } else {
            throw new UnsupportedDocumentTypeException(contractPatchDto.getDocumentType(), List.of(EWB));
        }
    }

    @Override
    @Transactional
    public void createTariff(AbstractTariffPostDto tariffPostDto, String humanReadableId) {
        var contract = contractRepository.findById(tariffPostDto.getContractId())
                .orElseThrow(() -> new ContractNotFoundException(tariffPostDto.getContractId()));
        var active = isContractActive(contract.getStart(), contract.getEnd());
        var tariff = tariffService.create(tariffPostDto, humanReadableId, active);
        if (tariffPostDto.getDocumentType().equals(EWB)) {
            ewbContractService.createTariff((EwbTariffPostDto) tariffPostDto, tariff);
        } else {
            throw new UnsupportedDocumentTypeException(tariffPostDto.getDocumentType(), List.of(EWB));
        }
    }

    @Override
    public AbstractGetContractByIdDto get(UUID id) {
        var type = contractGetInternalService.getContractType(id);
        if (EWB.equals(type)) {
            return ewbContractService.get(id);
        } else {
            throw new UnsupportedDocumentTypeException(type, List.of(EWB));
        }
    }

    @Override
    public List<Contract> getAllStarted() {
        return contractRepository.findStartedContracts();
    }

    @Override
    public List<Contract> getAllEnded() {
        return contractRepository.findEndedContracts();
    }

    @Override
    public void editTariff(UUID tariffId, AbstractTariffPatchDto abstractTariffPatchDto) {
        var tariff = tariffService.get(tariffId);
        var contract = contractRepository.findById(tariff.getContractId())
                .orElseThrow(() -> new ContractNotFoundException(tariff.getContractId()));
        if (!contract.isActive()) {
            throw new ContractNotActiveException(EDIT_TARIFF_OF_NOT_ACTIVE_CONTRACT_ERROR_MSG, tariff.getContractId());
        }
        tariffService.edit(tariffId, abstractTariffPatchDto);
    }

    @Override
    public void deactivate(UUID id) {
        var type = contractGetInternalService.getContractType(id);
        if (type.equals(EWB)) {
            ewbContractService.deactivate(id);
        } else {
            throw new UnsupportedDocumentTypeException(type, List.of(EWB));
        }
    }

    @Override
    public ContractWithServicePointsFileDto getContractSelfWithFuelStationPointsFile(UUID id, UUID userId) {
        var employee = employeeService.getByUserId(userId);
        var type = contractGetInternalService.getContractType(id);
        return switch (type) {
            case REPAIR_AND_MAINTENANCE ->
                    repairContractService.getContractSelfWithServicePointsFile(id, employee.getOrganization().getId());
            case FUEL ->
                    fuelContractService.getContractSelfWithServicePointsFile(id, employee.getOrganization().getId());
            default -> throw new UnexpectedDocumentTypeValidationException(CONTRACT_TYPE_MSG_FORMAT, type);
        };
    }

    @Override
    public ContractWithServicePointsFileDto getContractAllWithFuelStationPointsFile(UUID id) {
        var type = contractGetInternalService.getContractType(id);
        return switch (type) {
            case REPAIR_AND_MAINTENANCE -> repairContractService.getContractWithServicePointsFile(id);
            case FUEL -> fuelContractService.getContractWithServicePointsFile(id);
            default -> throw new UnexpectedDocumentTypeValidationException(CONTRACT_TYPE_MSG_FORMAT, type);
        };
    }

    @Override
    public UploadServicePointsDto validateServicePointsFileAllOrganizations(UUID contractId, DocumentType documentType, MultipartFile file) {
        switch (documentType) {
            case REPAIR_AND_MAINTENANCE -> {
                return repairContractService.validateServicePointsFileAllOrganizations(contractId, file);
            }
            case FUEL -> {
                return fuelContractService.validateServicePointsFileAllOrganizations(contractId, file);
            }
            default -> throw new UnsupportedDocumentTypeException(documentType, List.of(REPAIR_AND_MAINTENANCE, FUEL));
        }
    }

    @Override
    public UploadServicePointsDto validateServicePointsFileSelfOrganization(
            UUID contractId, DocumentType documentType, MultipartFile file, UUID userId) {
        var organizationId = employeeService.getByUserId(userId).getOrganization().getId();
        switch (documentType) {
            case REPAIR_AND_MAINTENANCE -> {
                return repairContractService.validateServicePointsFileSelfOrganization(contractId, file, organizationId);
            }
            case FUEL -> {
                return fuelContractService.validateServicePointsFileSelfOrganization(contractId, file, organizationId);
            }
            default -> throw new UnsupportedDocumentTypeException(documentType, List.of(REPAIR_AND_MAINTENANCE, FUEL));
        }
    }

    @Override
    @Transactional
    public void updatePartiallyContractSelfOrganization(UUID id, AbstractContractUpdateRequest request, UUID userId) {
        var employee = employeeService.getByUserId(userId);
        var type = contractGetInternalService.getContractType(id);
        if (!type.equals(request.getDocumentType())) {
            throw new ContractNotFoundException(request.getDocumentType(), id);
        }

        switch (type) {
            case REPAIR_AND_MAINTENANCE -> repairContractService.updatePartiallySelfOrganization(id,
                    repairContractMapper.toPartiallyUpdateRepairContractModel((RepairContractUpdateDto) request),
                    employee.getOrganization().getId());
            case FUEL -> fuelContractService.updatePartiallySelfOrganization(id,
                    fuelContractMapper.toPartiallyUpdateFuelContractModel((FuelContractUpdateDto) request),
                    employee.getOrganization().getId());
            default -> throw new UnexpectedDocumentTypeValidationException(CONTRACT_TYPE_MSG_FORMAT, type);
        }

    }

    @Override
    @Transactional
    public void updatePartiallyContractAllOrganizations(UUID id, AbstractContractUpdateRequest request) {
        var type = contractGetInternalService.getContractType(id);
        if (!type.equals(request.getDocumentType())) {
            throw new ContractNotFoundException(request.getDocumentType(), id);
        }

        switch (type) {
            case REPAIR_AND_MAINTENANCE -> repairContractService.updatePartiallyAllOrganizations(id,
                    repairContractMapper.toPartiallyUpdateRepairContractModel((RepairContractUpdateDto) request));
            case FUEL -> fuelContractService.updatePartiallyAllOrganizations(id,
                    fuelContractMapper.toPartiallyUpdateFuelContractModel((FuelContractUpdateDto) request));
            default -> throw new UnexpectedDocumentTypeValidationException(CONTRACT_TYPE_MSG_FORMAT, type);
        }
    }

    @Override
    @Transactional
    public void deactivateSelfOrganization(UUID id, UUID userId) {
        var employee = employeeService.getByUserId(userId);
        var type = contractGetInternalService.getContractType(id);
        switch (type) {
            case REPAIR_AND_MAINTENANCE ->
                    repairContractService.deactivateSelfOrganization(id, employee.getOrganization().getId());
            case FUEL -> fuelContractService.deactivateSelfOrganization(id, employee.getOrganization().getId());
            default -> throw new UnexpectedDocumentTypeValidationException(CONTRACT_TYPE_MSG_FORMAT, type);
        }
    }

    @Override
    @Transactional
    public void deactivateAllOrganizations(UUID id) {
        var type = contractGetInternalService.getContractType(id);
        switch (type) {
            case REPAIR_AND_MAINTENANCE -> repairContractService.deactivateAllOrganizations(id);
            case FUEL -> fuelContractService.deactivateAllOrganizations(id);
            default -> throw new UnexpectedDocumentTypeValidationException(CONTRACT_TYPE_MSG_FORMAT, type);
        }
    }

    @Override
    public boolean haveActiveContractsByContractorId(UUID contractorId) {
        var isAnyContracts = false;
        isAnyContracts = repairContractService.haveActiveContractsByContractorId(contractorId);
        if (!isAnyContracts) {
            isAnyContracts = fuelContractService.haveActiveContractsByContractorId(contractorId);
        }
        return isAnyContracts;
    }

    private boolean isContractActive(LocalDate start, LocalDate end) {
        var now = LocalDate.now(clock);
        var nowIsAfterOrEqualStart = now.isAfter(start) || now.isEqual(start);
        var nowIsBeforeOrEqualEnd = now.isBefore(end) || now.isEqual(end);
        return nowIsAfterOrEqualStart && nowIsBeforeOrEqualEnd;
    }
}
