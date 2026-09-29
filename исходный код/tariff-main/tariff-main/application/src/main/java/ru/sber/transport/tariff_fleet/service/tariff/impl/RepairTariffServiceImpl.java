package ru.sber.transport.tariff_fleet.service.tariff.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.tariff_fleet.constant.ActivationType;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.dao.RepairTariffRepository;
import ru.sber.transport.tariff_fleet.database.model.*;
import ru.sber.transport.tariff_fleet.dto.AbstractTariffResponse;
import ru.sber.transport.tariff_fleet.dto.SearchTariffResponse;
import ru.sber.transport.tariff_fleet.dto.repair.CreateRepairTariffDto;
import ru.sber.transport.tariff_fleet.exception.ContractorOrganizationNotFoundException;
import ru.sber.transport.tariff_fleet.exception.TariffNotFoundException;
import ru.sber.transport.tariff_fleet.mapper.RepairTariffMapper;
import ru.sber.transport.tariff_fleet.mapper.TariffMapper;
import ru.sber.transport.tariff_fleet.messaging.sender.RepairTariffSender;
import ru.sber.transport.tariff_fleet.model.TariffFilter;
import ru.sber.transport.tariff_fleet.service.OrganizationService;
import ru.sber.transport.tariff_fleet.service.SubContractInternalService;
import ru.sber.transport.tariff_fleet.service.tariff.RepairTariffService;
import ru.sber.transport.tariff_fleet.service.tariff.TariffValidationService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RepairTariffServiceImpl implements RepairTariffService {
    private final SubContractInternalService<RepairContract> repairContractInternalService;
    private final OrganizationService organizationService;
    private final TariffValidationService tariffValidationService;
    private final RepairTariffRepository repairTariffRepository;
    private final TariffMapper tariffMapper;
    private final RepairTariffMapper repairTariffMapper;
    private final RepairTariffSender repairTariffSender;

    @Override
    @Transactional
    public void createTariff(CreateRepairTariffDto request, Tariff tariff, Contract contract, UUID organizationId) {
        tariffValidationService.validateCreateRepairTariff(repairTariffRepository.findFirstByTariff_ContractIdAndTariff_ActiveIsTrue(request.getContractId()));
        var repairTariff = tariffMapper.createRepairTariffRequestToRepairTariff(request, tariff, contract, organizationId);
        repairTariffRepository.save(repairTariff);
        repairTariffSender.send(repairTariffMapper.toRepairTariffMessage(repairTariff, true));
    }

    @Override
    @Transactional(readOnly = true)
    public AbstractTariffResponse getTariff(UUID id, UUID organizationId) {
        var tariff = get(id);
        tariffValidationService.validateOrganizationPermission(id, tariff.getOrganizationId(), organizationId);
        var contract = repairContractInternalService.getContract(tariff.getTariff().getContractId());
        var organization = organizationService.get(tariff.getOrganizationId()).orElseThrow(() -> new ContractorOrganizationNotFoundException(id));
        return tariffMapper.repairTariffToRepairTariffDto(tariff, contract, organization.getOfficialName());
    }

    @Override
    @Transactional(readOnly = true)
    public AbstractTariffResponse getTariff(UUID id) {
        var tariff = get(id);
        var contract = repairContractInternalService.getContract(tariff.getTariff().getContractId());
        var organization = organizationService.get(tariff.getOrganizationId()).orElseThrow(() -> new ContractorOrganizationNotFoundException(id));
        return tariffMapper.repairTariffToRepairTariffDto(tariff, contract, organization.getOfficialName());
    }

    @Override
    @Transactional
    public void deactivateTariff(UUID id, UUID organizationId) {
        var tariff = get(id);
        tariffValidationService.validateOrganizationPermission(id, tariff.getOrganizationId(), organizationId);
        tariff.getTariff().setActive(false);
        tariff.getTariff().setActivationType(ActivationType.MANUAL);
        repairTariffRepository.save(tariff);
        repairTariffSender.send(repairTariffMapper.toRepairTariffMessage(tariff, false));
    }

    @Override
    @Transactional
    public void deactivateTariff(UUID id) {
        var tariff = get(id);
        tariff.getTariff().setActive(false);
        tariff.getTariff().setActivationType(ActivationType.MANUAL);
        repairTariffRepository.save(tariff);
        repairTariffSender.send(repairTariffMapper.toRepairTariffMessage(tariff, false));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SearchTariffResponse> searchTariff(TariffFilter tariffFilter, Pageable paging) {
        return repairTariffRepository.searchByFilter(tariffFilter, paging)
                .map( it -> tariffMapper.getTariffProjectionToSearchTariffResponse(it, DocumentType.REPAIR_AND_MAINTENANCE));
    }

    @Override
    @Transactional
    public Organization getOrganizationByContract(UUID contractId) {
        return repairContractInternalService.getContract(contractId).getOrganization();
    }

    private RepairTariff get(UUID id) {
        return repairTariffRepository.findById(id).orElseThrow(() -> new TariffNotFoundException(id));
    }
}
