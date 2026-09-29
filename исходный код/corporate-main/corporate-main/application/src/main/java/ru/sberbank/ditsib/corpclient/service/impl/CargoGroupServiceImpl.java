package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.corpclient.database.dao.CargoGroupRepository;
import ru.sberbank.ditsib.corpclient.database.dao.CargoTypeRepository;
import ru.sberbank.ditsib.corpclient.database.model.CargoGroup;
import ru.sberbank.ditsib.corpclient.service.CargoGroupService;

import java.util.Collection;
import java.util.UUID;

/**
 * Implementation of cargo group service
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CargoGroupServiceImpl implements CargoGroupService {

    private final CargoGroupRepository repository;
    private final CargoTypeRepository typeRepository;

    @Override
    public Collection<CargoGroup> getTypesByGroupName(String groupName) {
        return repository.findAllByGargoGroupName(groupName);
    }

    @Override
    public CargoGroup save(CargoGroup cargoGroup) {
        return repository.save(cargoGroup);
    }

    @Override
    public void delete(UUID groupId) {
        repository.findById(groupId)
                .ifPresent(row-> {
                    row.getCargoType().getCargoGroups().remove(row);
                    typeRepository.save(row.getCargoType());
                });
    }
}
