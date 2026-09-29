package ru.sberbank.ditsib.corpclient.controller.cargo.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sberbank.ditsib.corpclient.controller.cargo.CargoTypeController;
import ru.sberbank.ditsib.corpclient.database.model.CargoType;
import ru.sberbank.ditsib.corpclient.dto.cargo.CargoTypeCategoryDto;
import ru.sberbank.ditsib.corpclient.dto.cargo.CargoTypeDto;
import ru.sberbank.ditsib.corpclient.dto.mapper.CargoTypeMapper;
import ru.sberbank.ditsib.corpclient.exceptions.CargoCategoryException;
import ru.sberbank.ditsib.corpclient.messaging.sender.CargoTypeSender;
import ru.sberbank.ditsib.corpclient.service.CargoTypeService;
import ru.sberbank.ditsib.corpclient.service.EmployeeService;
import ru.sberbank.ditsib.corpclient.service.OrganizationService;
import ru.sberbank.ditsib.transport.constants.cargo.CargoCategoryEnum;
import ru.sberbank.ditsib.transport.constants.cargo.CargoTypeEnum;

import java.util.*;

@RequiredArgsConstructor
@RestController
class CargoTypeControllerImpl implements CargoTypeController {
    private final CargoTypeService service;
    private final CargoTypeSender sender;
    private final CargoTypeMapper mapper;
    private final OrganizationService organizationService;
    private final EmployeeService employeService;

    private static final String DATA_MASTER = "data_master";

    @Override
    @CheckOrganizationAccess
    public CargoTypeDto getType(@Organization UUID organizationId, UUID typeId, JwtAuthenticationToken authentication) {
        var cargoType = getTypeWithOrganizationCheck(typeId, organizationId, authentication);

        return mapper.entityToDto(cargoType);
    }

    @Override
    @CheckOrganizationAccess
    public CargoTypeDto addType(@Organization UUID organizationId, CargoTypeDto newData,
                                JwtAuthenticationToken authentication) {

        throwIfCargoCategoryOTHER(newData.getCategory());

        var cargoType = mapper.newDtoToEntity(newData);
        boolean dataMaster = Optional.ofNullable(authentication.getToken().getClaimAsBoolean(DATA_MASTER))
                .orElse(false);

        if (!dataMaster || !newData.isUniversal()) {
            var organization = organizationService.get(organizationId);
            cargoType.setOrganization(organization);
        }

        cargoType = service.save(cargoType);
        sender.send(cargoType);
        return mapper.entityToDto(cargoType);
    }

    @Override
    public CargoTypeDto addType(CargoTypeDto newData, JwtAuthenticationToken authentication) {
        throwIfCargoCategoryOTHER(newData.getCategory());

        var userId = UUID.fromString(authentication.getToken().getId());
        var employee = employeService.getEmployeeByUserId(userId);

        var cargoType = mapper.newDtoToEntity(newData);
        cargoType.setOrganization(employee.getOrganization());

        cargoType = service.save(cargoType);
        sender.send(cargoType);
        return mapper.entityToDto(cargoType);
    }

    @Override
    @CheckOrganizationAccess
    public CargoTypeDto updateType(@Organization UUID organizationId, UUID typeId, CargoTypeDto data,
                                   JwtAuthenticationToken authentication) {

        throwIfCargoCategoryOTHER(data.getCategory());

        CargoType cargoType = getTypeWithOrganizationCheck(typeId, organizationId, authentication);
        boolean dataMaster = Optional.ofNullable(authentication.getToken().getClaimAsBoolean(DATA_MASTER))
                .orElse(false);

        if (dataMaster) {
            if (data.isUniversal()) {
                cargoType.setOrganization(null);
            } else {
                var organization = organizationService.get(organizationId);
                cargoType.setOrganization(organization);
            }
        }
        var cargoGroups = cargoType.getCargoGroups().stream()
                .peek(elt -> {elt.setId(null); elt.setCargoType(null);})
                        .toList();
        service.deActivation(typeId);
        data.setId(null);
        CargoType toSave = mapper.newDtoToEntity(data);
        toSave.setOrganization(cargoType.getOrganization());
       cargoGroups.stream()
                .forEach(elt -> {
                    toSave.getCargoGroups().add(elt);
                    elt.setCargoType(toSave);
                });

        cargoType = service.save(toSave);
        sender.sendDeleted(typeId);
        sender.send(cargoType);
        return mapper.entityToDto(cargoType);
    }

    @Override
    @CheckOrganizationAccess
    public void deleteType(@Organization UUID organizationId, UUID typeId, JwtAuthenticationToken authentication) {
        CargoType cargoType = getTypeWithOrganizationCheck(typeId, organizationId, authentication);

        service.deActivation(cargoType.getId());
        sender.sendDeleted(cargoType.getId());
    }

    @SuppressWarnings("java:S3958")
    @Override
    @CheckOrganizationAccess
    public Collection<CargoTypeDto> getTypes(@Organization UUID organizationId, JwtAuthenticationToken authentication) {
        boolean dataMaster = Optional.ofNullable(authentication.getToken().getClaimAsBoolean(DATA_MASTER))
                .orElse(false);


        List<CargoType> cargoTypes = dataMaster
                ? service.getAllActive()
                : service.getAllActiveByOrganizationId(organizationId);

        return cargoTypes
                .stream()
                .map(mapper::entityToDto)
                .toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<CargoTypeCategoryDto> getAllTypes() {
        return Arrays.stream(CargoTypeEnum.values())
                .map(elt -> CargoTypeCategoryDto.builder()
                        .name(elt.name())
                        .value(elt.getName())
                        .id(elt.getId())
                        .build())
                .toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<CargoTypeCategoryDto> getAllCategory() {
        return Arrays.stream(CargoCategoryEnum.values())
                .filter(c -> c != CargoCategoryEnum.OTHER)
                .map(elt -> CargoTypeCategoryDto.builder()
                        .name(elt.name())
                        .value(elt.getName())
                        .id(elt.getId())
                        .unit(elt.getUnit())
                        .build())
                .toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    @CheckOrganizationAccess
    public List<CargoTypeDto> search(@Organization UUID organizationId, String searchText,
                                     JwtAuthenticationToken authentication) {

        boolean dataMaster = Optional.ofNullable(authentication.getToken().getClaimAsBoolean(DATA_MASTER))
                .orElse(false);

        List<CargoType> cargoTypes = dataMaster
                ? service.search(searchText)
                : service.search(organizationId, searchText);

        return cargoTypes
                .stream()
                .map(mapper::entityToDto)
                .toList();
    }

    @Override
    public List<CargoTypeDto> search(String searchText, JwtAuthenticationToken authentication) {
        boolean dataMaster = Optional.ofNullable(authentication.getToken().getClaimAsBoolean(DATA_MASTER))
                .orElse(false);

        List<CargoType> cargoTypes;
        if (!dataMaster) {
            var userId = UUID.fromString(authentication.getToken().getId());
            var employee = employeService.getEmployeeByUserId(userId);
            UUID organizationId = employee.getOrganization().getId();
            cargoTypes = service.search(organizationId, searchText);
        } else {
            cargoTypes = service.search(searchText);
        }

        return cargoTypes
                .stream()
                .map(mapper::entityToDto)
                .toList();
    }

    private CargoType getTypeWithOrganizationCheck(UUID typeId, UUID organizationId, JwtAuthenticationToken authentication) {
        boolean dataMaster = Optional.ofNullable(authentication.getToken().getClaimAsBoolean(DATA_MASTER))
                .orElse(false);

        return dataMaster
                ? service.getActive(typeId)
                : service.getActiveByOrganizationId(typeId, organizationId);
    }

    private void throwIfCargoCategoryOTHER(CargoCategoryEnum cargoCategory) {
        if (cargoCategory == CargoCategoryEnum.OTHER) {
            throw new CargoCategoryException(cargoCategory);
        }
    }
}
