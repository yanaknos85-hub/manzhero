package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.corpclient.database.dao.CargoTypeRepository;
import ru.sberbank.ditsib.corpclient.database.model.CargoType;
import ru.sberbank.ditsib.corpclient.service.CargoTypeService;

import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.constants.cargo.CargoCategoryEnum;

import java.util.List;
import java.util.UUID;

import static ru.sberbank.ditsib.transport.constants.cargo.CargoCategoryEnum.BULK;
import static ru.sberbank.ditsib.transport.constants.cargo.CargoCategoryEnum.LIQUID;

/**
 * Implementation of cargo package service
 */
@Service
@Transactional
@RequiredArgsConstructor
class CargoTypeServiceImpl implements CargoTypeService {

    private final CargoTypeRepository repository;

    private static final List<CargoCategoryEnum> categoriesWithoutDimensions = List.of(LIQUID, BULK);

    @Override
    public CargoType getActive(UUID uuid) {
        return repository.findByIdAndActiveTrue(uuid)
                .orElseThrow(() -> new EntityNotFoundException(CargoType.class, uuid));
    }

    @Override
    public CargoType getActiveByOrganizationId(UUID id, UUID organizationId) {
        return repository.findByIdAndOrganizationIdAndActiveTrue(id, organizationId)
                .orElseThrow(() -> new EntityNotFoundException(CargoType.class, id));
    }

    @Override
    public List<CargoType> getAllActiveByOrganizationId(UUID organizationId) {
        return repository.findAllByOrganizationIdAndActive(organizationId);
    }

    @Override
    public List<CargoType> getAllActive() {
        return repository.findAllByActiveTrue();
    }

    @Override
    public CargoType save(CargoType cargoType) {
        checkDuplicate(cargoType.getName());
        if(!categoriesWithoutDimensions.contains(cargoType.getCategory())) {
            calculateVolume(cargoType);
        }
        return repository.save(cargoType);
    }

    @Override
    public void deActivation(@NotNull UUID uuid) {
        var cargoType = repository.findById(uuid).
                orElseThrow(() -> new EntityNotFoundException(CargoType.class, uuid));
        cargoType.setActive(false);
        cargoType.getCargoGroups().clear();
        repository.save(cargoType);
    }

    @Override
    public CargoType getByName(String name) {
        return repository.findByNameAndActive(name).orElse(null);
    }

    @Override
    public List<CargoType> search(String searchText) {
        return repository.findAllByNameLikeAndActiveTrue(searchText);
    }

    @Override
    public List<CargoType> searchWithEmptyOrganization(String searchText) {
        return repository.findAllByEmptyOrganizationAndNameLikeAndActiveTrue(searchText);
    }

    @Override
    public List<CargoType> search(UUID organizationId, String searchText) {
        return repository.findAllByOrganizationAndNameLikeAndActiveTrue(organizationId, searchText);
    }

    private void checkDuplicate(String name) {
        repository.findByNameAndActive(name)
                .ifPresent(p -> {
                    throw new DuplicateDataException(CargoType.class, "name", p.getName());
                });
    }

    private void calculateVolume(CargoType cargoType) {
        cargoType.setVolume(cargoType.getHeight()
                * cargoType.getLength()
                * cargoType.getWidth());
    }
}
