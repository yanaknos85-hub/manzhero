package ru.sber.transport.tariff_fleet.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.tariff_fleet.controller.TariffController;
import ru.sber.transport.tariff_fleet.dto.*;
import ru.sber.transport.tariff_fleet.helper.UserAuthorizationHelper;
import ru.sber.transport.tariff_fleet.service.ContractService;
import ru.sber.transport.tariff_fleet.service.HumanReadableIdService;
import ru.sber.transport.tariff_fleet.service.tariff.TariffService;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class TariffControllerImpl implements TariffController {

    private final TariffService tariffService;
    private final ContractService contractService;
    private final HumanReadableIdService humanReadableIdService;

    @Override
    public void create(AbstractTariffPostDto abstractTariffPostDto, Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        var humanReadableId = humanReadableIdService.createHumanReadableIdByUserId(userId);
        contractService.createTariff(abstractTariffPostDto, humanReadableId);
    }

    @Override
    public Page<? extends AbstractTariffGetDto> search(AbstractSearchTariffDto abstractSearchTariffDto) {
        return tariffService.search(abstractSearchTariffDto);
    }

    @Override
    public AbstractTariffGetByIdDto getById(UUID id) {
        return tariffService.getById(id);
    }

    @Override
    public void edit(UUID id, AbstractTariffPatchDto abstractTariffPatchDto) {
        contractService.editTariff(id, abstractTariffPatchDto);
    }

    @Override
    public void deactivate(UUID id) {
        tariffService.deactivate(id);
    }

    @Override
    public void createTariffSelfOrganization(AbstractCreateTariffRequest request, Authentication authentication) {
        tariffService.createTariffSelfOrganization(request, UserAuthorizationHelper.getUserId(authentication));
    }

    @Override
    public void createTariffAllOrganizations(AbstractCreateTariffRequest request) {
        tariffService.createTariffAllOrganizations(request);
    }

    @Override
    public AbstractTariffResponse getTariffSelfOrganization(UUID id, Authentication authentication) {
        return tariffService.getTariffSelfOrganization(id, UserAuthorizationHelper.getUserId(authentication));
    }

    @Override
    public AbstractTariffResponse getTariffAllOrganizations(UUID id) {
        return tariffService.getTariffAllOrganizations(id);
    }

    @Override
    public void deactivateTariffSelfOrganization(UUID id, Authentication authentication) {
        tariffService.deactivateTariffSelfOrganization(id, UserAuthorizationHelper.getUserId(authentication));
    }

    @Override
    public void deactivateTariffAllOrganizations(UUID id) {
        tariffService.deactivateTariffAllOrganizations(id);
    }

    @Override
    public Page<SearchTariffResponse> searchTariffSelfOrganization(SearchTariffRequest request, Authentication authentication) {
        return tariffService.searchTariffSelfOrganization(request, UserAuthorizationHelper.getUserId(authentication));
    }

    @Override
    public Page<SearchTariffResponse> searchTariffAllOrganizations(SearchTariffRequest request) {
        return tariffService.searchTariffAllOrganizations(request);
    }
}
