package ru.sber.transport.tariff_fleet.service.tariff.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.dao.TariffRepository;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.Tariff;
import ru.sber.transport.tariff_fleet.dto.*;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbSearchTariffDto;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbTariffPatchDto;
import ru.sber.transport.tariff_fleet.dto.fuel.CreateFuelTariffDto;
import ru.sber.transport.tariff_fleet.dto.repair.CreateRepairTariffDto;
import ru.sber.transport.tariff_fleet.exception.ContractNotFoundException;
import ru.sber.transport.tariff_fleet.exception.TariffNotFoundException;
import ru.sber.transport.tariff_fleet.exception.UnsupportedDocumentTypeException;
import ru.sber.transport.tariff_fleet.mapper.TariffMapper;
import ru.sber.transport.tariff_fleet.service.ContractGetInternalService;
import ru.sber.transport.tariff_fleet.service.EmployeeService;
import ru.sber.transport.tariff_fleet.service.HumanReadableIdService;
import ru.sber.transport.tariff_fleet.service.grpc.FuelGrpcService;
import ru.sber.transport.tariff_fleet.service.tariff.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static ru.sber.transport.tariff_fleet.constant.DocumentType.*;

@Service
@RequiredArgsConstructor
public class TariffServiceImpl implements TariffService {

    private final EwbTariffService ewbTariffService;
    private final TariffRepository tariffRepository;
    private final TariffMapper tariffMapper;
    private final ContractGetInternalService contractGetInternalService;
    private final TariffValidationService tariffValidationService;
    private final RepairTariffService repairTariffService;
    private final FuelTariffService fuelTariffService;
    private final EmployeeService employeeService;
    private final HumanReadableIdService humanReadableIdService;
    private final FuelGrpcService fuelGrpcService;

    @Override
    @Transactional
    public Tariff create(AbstractTariffPostDto postTariffDto, String humanReadableId, boolean active) {
        return tariffRepository.save(tariffMapper.abstractPostTariffDtoToTariff(postTariffDto, humanReadableId, active));
    }

    @Override
    public Page<? extends AbstractTariffGetDto> search(AbstractSearchTariffDto abstractSearchTariffDto) {
        if (abstractSearchTariffDto.getDocumentType().equals(EWB)) {
            return ewbTariffService.search((EwbSearchTariffDto) abstractSearchTariffDto);
        } else {
            throw new UnsupportedDocumentTypeException(abstractSearchTariffDto.getDocumentType(), List.of(EWB));
        }
    }

    @Override
    public Tariff get(UUID tariffId) {
        return tariffRepository.findById(tariffId)
                .orElseThrow(() -> new TariffNotFoundException(tariffId));
    }

    @Override
    public AbstractTariffGetByIdDto getById(UUID id) {
        var type = tariffRepository.getTypeById(id)
                .orElseThrow(() -> new TariffNotFoundException(id));
        if (type.equals(EWB)) {
            return ewbTariffService.get(id);
        } else {
            throw new UnsupportedDocumentTypeException(type, List.of(EWB));
        }
    }

    @Override
    public List<Tariff> getAllByContractId(UUID contractId) {
        return tariffRepository.findAllByContractId(contractId);
    }

    @Override
    public void edit(UUID tariffId, AbstractTariffPatchDto abstractTariffPatchDto) {
        if (Objects.equals(abstractTariffPatchDto.getDocumentType(), EWB)) {
            ewbTariffService.edit(tariffId, (EwbTariffPatchDto) abstractTariffPatchDto);
        } else {
            throw new UnsupportedDocumentTypeException(abstractTariffPatchDto.getDocumentType(), List.of(EWB));
        }
    }

    @Override
    public void deactivate(UUID id) {
        var type = getTariffType(id);
        if (type.equals(EWB)) {
            ewbTariffService.deactivate(id);
        } else {
            throw new UnsupportedDocumentTypeException(type, List.of(EWB));
        }
    }

    @Override
    @Transactional
    public void createTariffSelfOrganization(AbstractCreateTariffRequest request, UUID userId) {
        var contract = contractGetInternalService.getContract(request.getContractId()).orElseThrow(() -> new ContractNotFoundException(request.getContractId()));
        var user = employeeService.getByUserId(userId);
        tariffValidationService.validateCreateTariff(contract);
        var humanReadableId = humanReadableIdService.createHumanReadableIdByUserId(userId);
        var tariff = tariffRepository.save(tariffMapper.createTariffRequestToTariff(request, humanReadableId));
        createTariff(request, tariff, contract, user.getOrganization().getId());
    }

    @Override
    @Transactional
    public void createTariffAllOrganizations(AbstractCreateTariffRequest request) {
        var contract = contractGetInternalService.getContract(request.getContractId()).orElseThrow(() -> new ContractNotFoundException(request.getContractId()));
        tariffValidationService.validateCreateTariff(contract);
        var organization = getOrganizationFromContract(request.getDocumentType(), request.getContractId());
        var humanReadableId = humanReadableIdService.createHumanReadableIdByDigitId(organization.getDigitId());
        var tariff = tariffRepository.save(tariffMapper.createTariffRequestToTariff(request, humanReadableId));
        createTariff(request, tariff, contract, organization.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public AbstractTariffResponse getTariffSelfOrganization(UUID id, UUID userId) {
        var user = employeeService.getByUserId(userId);
        var tariffType = getTariffType(id);
        return switch (tariffType) {
            case REPAIR_AND_MAINTENANCE -> repairTariffService.getTariff(id, user.getOrganization().getId());
            case FUEL -> fuelTariffService.getTariff(id, user.getOrganization().getId());
            default -> throw new UnsupportedDocumentTypeException(tariffType, List.of(FUEL, REPAIR_AND_MAINTENANCE));
        };

    }

    @Override
    @Transactional(readOnly = true)
    public AbstractTariffResponse getTariffAllOrganizations(UUID id) {
        var tariffType = getTariffType(id);
        return switch (tariffType) {
            case REPAIR_AND_MAINTENANCE -> repairTariffService.getTariff(id);
            case FUEL -> fuelTariffService.getTariff(id);
            default -> throw new UnsupportedDocumentTypeException(tariffType, List.of(FUEL, REPAIR_AND_MAINTENANCE));
        };
    }

    @Override
    @Transactional
    public void deactivateTariffSelfOrganization(UUID id, UUID userId) {
        var user = employeeService.getByUserId(userId);
        var tariffType = getTariffType(id);
        var tariff = get(id);
        tariffValidationService.validateDeactivateTariff(tariff);
        switch (tariffType) {
            case REPAIR_AND_MAINTENANCE -> repairTariffService.deactivateTariff(id, user.getOrganization().getId());
            case FUEL -> fuelTariffService.deactivateTariff(id, user.getOrganization().getId());
            default -> throw new UnsupportedDocumentTypeException(tariffType, List.of(FUEL, REPAIR_AND_MAINTENANCE));
        }
    }

    @Override
    @Transactional
    public void deactivateTariffAllOrganizations(UUID id) {
        var tariffType = getTariffType(id);
        var tariff = get(id);
        tariffValidationService.validateDeactivateTariff(tariff);
        switch (tariffType) {
            case REPAIR_AND_MAINTENANCE -> repairTariffService.deactivateTariff(id);
            case FUEL -> fuelTariffService.deactivateTariff(id);
            default -> throw new UnsupportedDocumentTypeException(tariffType, List.of(FUEL, REPAIR_AND_MAINTENANCE));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SearchTariffResponse> searchTariffAllOrganizations(SearchTariffRequest request) {
        return switch (request.getDocumentType()) {
            case REPAIR_AND_MAINTENANCE -> repairTariffService.searchTariff(
                    tariffMapper.searchTariffRequestToTariffFilter(request),
                    PageRequest.of(request.getPageSetting().getPage(), request.getPageSetting().getSize(), request.getSort())
            );
            case FUEL -> fuelTariffService.searchTariff(
                    tariffMapper.searchTariffRequestToTariffFilter(request),
                    PageRequest.of(request.getPageSetting().getPage(), request.getPageSetting().getSize(), request.getSort())
            );
            default ->
                    throw new UnsupportedDocumentTypeException(request.getDocumentType(), List.of(FUEL, REPAIR_AND_MAINTENANCE));
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SearchTariffResponse> searchTariffSelfOrganization(SearchTariffRequest request, UUID userId) {
        var user = employeeService.getByUserId(userId);
        return switch (request.getDocumentType()) {
            case REPAIR_AND_MAINTENANCE -> repairTariffService.searchTariff(
                    tariffMapper.searchTariffRequestToTariffFilter(request, user.getOrganization().getId()),
                    PageRequest.of(request.getPageSetting().getPage(), request.getPageSetting().getSize(), request.getSort()));
            case FUEL -> fuelTariffService.searchTariff(
                    tariffMapper.searchTariffRequestToTariffFilter(request, user.getOrganization().getId()),
                    PageRequest.of(request.getPageSetting().getPage(), request.getPageSetting().getSize(), request.getSort()));
            default ->
                    throw new UnsupportedDocumentTypeException(request.getDocumentType(), List.of(FUEL, REPAIR_AND_MAINTENANCE));
        };
    }

    @Override
    @Transactional
    public void activateByContractId(UUID contractId) {
        tariffRepository.activateByContractId(contractId);
    }

    @Override
    @Transactional
    public void deactivateByContractId(UUID contractId) {
        fuelGrpcService.deactivateFuelCardByContractId(contractId);
        tariffRepository.deactivateByContractId(contractId);
    }

    private DocumentType getTariffType(UUID id) {
        return tariffRepository.getTypeById(id)
                .orElseThrow(() -> new TariffNotFoundException(id));
    }

    private void createTariff(AbstractCreateTariffRequest request, Tariff tariff, Contract contract, UUID organizationId) {
        switch (request.getDocumentType()) {
            case REPAIR_AND_MAINTENANCE -> repairTariffService.createTariff(
                    (CreateRepairTariffDto) request,
                    tariff,
                    contract,
                    organizationId
            );
            case FUEL -> fuelTariffService.createTariff(
                    (CreateFuelTariffDto) request,
                    tariff,
                    contract,
                    organizationId
            );
            case EWB, ELECTRIC_FUEL ->
                    throw new UnsupportedDocumentTypeException(request.getDocumentType(), List.of(FUEL, REPAIR_AND_MAINTENANCE));
        }
    }

    private Organization getOrganizationFromContract(DocumentType documentType, UUID contractId) {
        return switch (documentType) {
            case REPAIR_AND_MAINTENANCE -> repairTariffService.getOrganizationByContract(contractId);
            case FUEL -> fuelTariffService.getOrganizationByContract(contractId);
            case EWB, ELECTRIC_FUEL ->
                    throw new UnsupportedDocumentTypeException(documentType, List.of(FUEL, REPAIR_AND_MAINTENANCE));
        };
    }
}