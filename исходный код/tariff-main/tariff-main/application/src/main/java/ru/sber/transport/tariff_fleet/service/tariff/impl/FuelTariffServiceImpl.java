package ru.sber.transport.tariff_fleet.service.tariff.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.tariff_fleet.constant.ActivationType;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.dao.FuelTariffRepository;
import ru.sber.transport.tariff_fleet.database.dao.TariffRepository;
import ru.sber.transport.tariff_fleet.database.model.*;
import ru.sber.transport.tariff_fleet.dto.AbstractTariffResponse;
import ru.sber.transport.tariff_fleet.dto.SearchTariffResponse;
import ru.sber.transport.tariff_fleet.dto.fuel.CreateFuelTariffDto;
import ru.sber.transport.tariff_fleet.exception.ContractorOrganizationNotFoundException;
import ru.sber.transport.tariff_fleet.exception.DepartmentNotActiveException;
import ru.sber.transport.tariff_fleet.exception.TariffNotFoundException;
import ru.sber.transport.tariff_fleet.mapper.TariffMapper;
import ru.sber.transport.tariff_fleet.messaging.sender.FuelTariffSender;
import ru.sber.transport.tariff_fleet.model.TariffFilter;
import ru.sber.transport.tariff_fleet.service.DepartmentService;
import ru.sber.transport.tariff_fleet.service.OrganizationService;
import ru.sber.transport.tariff_fleet.service.SubContractInternalService;
import ru.sber.transport.tariff_fleet.service.grpc.FuelGrpcService;
import ru.sber.transport.tariff_fleet.service.tariff.FuelTariffService;
import ru.sber.transport.tariff_fleet.service.tariff.TariffValidationService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FuelTariffServiceImpl implements FuelTariffService {

    private final OrganizationService organizationService;
    private final DepartmentService departmentService;
    private final TariffValidationService tariffValidationService;
    private final FuelTariffRepository fuelTariffRepository;
    private final TariffMapper tariffMapper;
    private final TariffRepository tariffRepository;
    private final SubContractInternalService<FuelContract> fuelContractInternalService;
    private final FuelTariffSender fuelTariffSender;
    private final FuelGrpcService fuelGrpcService;

    @Override
    @Transactional
    public void createTariff(CreateFuelTariffDto request, Tariff tariff, Contract contract, UUID organizationId) {
        var department = departmentService.get(request.getDepartmentId()).orElseThrow(() -> new DepartmentNotActiveException(request.getDepartmentId()));
        var fuelContract = fuelContractInternalService.getContract(contract.getId());
        tariffValidationService.validateCreateFuelTariff(
                fuelTariffRepository.findFirstByTariff_ContractIdAndDepartmentIdAndTariff_ActiveIsTrue(
                        request.getContractId(),
                        request.getDepartmentId()
                ),
                department,
                contract,
                fuelContract.getOrganization()
        );
        var savedTariff = fuelTariffRepository.save(
                tariffMapper.createFuelTariffRequestToFuelTariff(request, tariff, department, contract, organizationId));
        fuelTariffSender.send(tariffMapper.toMessage(savedTariff));
    }

    @Override
    @Transactional(readOnly = true)
    public AbstractTariffResponse getTariff(UUID id, UUID organizationId) {
        var tariff = get(id);
        tariffValidationService.validateOrganizationPermission(id, tariff.getOrganizationId(), organizationId);
        var contract = fuelContractInternalService.getContract(tariff.getTariff().getContractId());
        var organization = organizationService.get(tariff.getOrganizationId()).orElseThrow(() -> new ContractorOrganizationNotFoundException(id));
        var department = departmentService.get(tariff.getDepartmentId()).orElseThrow(() -> new DepartmentNotActiveException(tariff.getDepartmentId()));
        return tariffMapper.fuelTariffToFuelTariffDto(tariff, contract, organization.getOfficialName(), department);
    }

    @Override
    @Transactional(readOnly = true)
    public AbstractTariffResponse getTariff(UUID id) {
        var tariff = get(id);
        var contract = fuelContractInternalService.getContract(tariff.getTariff().getContractId());
        var organization = organizationService.get(tariff.getOrganizationId()).orElseThrow(() -> new ContractorOrganizationNotFoundException(id));
        var department = departmentService.get(tariff.getDepartmentId()).orElseThrow(() -> new DepartmentNotActiveException(tariff.getDepartmentId()));
        return tariffMapper.fuelTariffToFuelTariffDto(tariff, contract, organization.getOfficialName(), department);
    }

    @Override
    @Transactional
    public void deactivateTariff(UUID id, UUID organizationId) {
        var tariff = get(id);
        tariffValidationService.validateOrganizationPermission(id, tariff.getOrganizationId(), organizationId);
        deactivateFuelCards(tariff);
        tariff.getTariff().setActive(false);
        tariff.getTariff().setActivationType(ActivationType.MANUAL);
        fuelTariffRepository.save(tariff);
        fuelTariffSender.send(tariffMapper.toMessage(tariff));
    }

    @Override
    @Transactional
    public void deactivateTariff(UUID id) {
        var tariff = get(id);
        deactivateFuelCards(tariff);
        tariff.getTariff().setActive(false);
        tariff.getTariff().setActivationType(ActivationType.MANUAL);
        fuelTariffRepository.save(tariff);
        fuelTariffSender.send(tariffMapper.toMessage(tariff));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SearchTariffResponse> searchTariff(TariffFilter tariffFilter, Pageable paging) {
        return fuelTariffRepository.searchByFilter(tariffFilter, paging)
                .map( it -> tariffMapper.getTariffProjectionToSearchTariffResponse(it, DocumentType.FUEL));
    }

    @Override
    @Transactional
    public void activateByContractId(UUID contractId) {
        tariffRepository.activateByContractId(contractId);
        var tariffs = fuelTariffRepository.findAllByTariff_ContractIdAndTariff_ActivationTypeNot(contractId, ActivationType.MANUAL);
        var messages = tariffs.stream().map(tariffMapper::toMessage).toList();
        messages.forEach(fuelTariffSender::send);
    }

    @Override
    @Transactional
    public void deactivateByContractId(UUID contractId) {
        tariffRepository.deactivateByContractId(contractId);
        var tariffs = fuelTariffRepository.findAllByTariff_ContractId(contractId);
        var messages = tariffs.stream().map(tariffMapper::toMessage).toList();
        messages.forEach(fuelTariffSender::send);
    }

    @Override
    @Transactional
    public Organization getOrganizationByContract(UUID contractId) {
        return fuelContractInternalService.getContract(contractId).getOrganization();
    }

    private FuelTariff get(UUID id) {
        return fuelTariffRepository.findById(id).orElseThrow(() -> new TariffNotFoundException(id));
    }

    private void deactivateFuelCards(FuelTariff fuelTariff) {
        var departmentId = fuelTariff.getDepartmentId();
        var contractId = fuelTariff.getTariff().getContractId();

        fuelGrpcService.deactivateFuelCardByContractAndDepartmentId(contractId, departmentId);
    }
}
