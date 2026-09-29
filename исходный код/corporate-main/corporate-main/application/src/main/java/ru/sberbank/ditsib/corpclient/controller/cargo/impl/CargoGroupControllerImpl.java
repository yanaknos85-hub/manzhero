package ru.sberbank.ditsib.corpclient.controller.cargo.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.corpclient.controller.cargo.CargoGroupController;
import ru.sberbank.ditsib.corpclient.dto.cargo.CagroGroupTypeDto;
import ru.sberbank.ditsib.corpclient.dto.cargo.CargoGroupDto;
import ru.sberbank.ditsib.corpclient.dto.mapper.CargoGroupMapper;
import ru.sberbank.ditsib.corpclient.service.CargoGroupService;
import ru.sberbank.ditsib.corpclient.service.CargoTypeService;

import java.util.Collection;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
class CargoGroupControllerImpl implements CargoGroupController {
    private final CargoGroupService service;
    private final CargoTypeService cargoTypeService;
    private final CargoGroupMapper mapper;

    private static final String DATA_MASTER = "data_master";

    @Override
    public Collection<CagroGroupTypeDto> getTypesByGroupName(String groupName, JwtAuthenticationToken authentication) {
        var cargoGroups = service.getTypesByGroupName(groupName);
        return cargoGroups
                .stream()
                .map(mapper::entityToDto)
                .toList();
    }

    @Override
    public void deleteGroup(UUID groupId, JwtAuthenticationToken authentication) {
        service.delete(groupId);
    }

    @Override
    public CagroGroupTypeDto addGroup(CargoGroupDto newData, JwtAuthenticationToken authentication) {
        var cargoType = cargoTypeService.getActive(newData.getCargoTypeId());
        var cargoGroup = mapper.newDtoToEntity(newData);
        cargoGroup.setCargoType(cargoType);
        cargoGroup = service.save(cargoGroup);
        return mapper.entityToDto(cargoGroup);
    }
}
