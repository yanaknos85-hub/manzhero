package ru.sberbank.ditsib.corpclient.service.cargo.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.corpclient.database.dao.CargoDeliveryTimeRepository;
import ru.sberbank.ditsib.corpclient.database.model.CargoDeliveryTime;
import ru.sberbank.ditsib.corpclient.service.cargo.CargoDeliveryTimeService;
import ru.sberbank.utils.reflection.ReflectionUtils;

import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of cargo package service
 */
@Service
@Transactional
@RequiredArgsConstructor
class CargoDeliveryTimeServiceImpl implements CargoDeliveryTimeService {
    
    private final CargoDeliveryTimeRepository repository;
    
    @Override
    public List<CargoDeliveryTime> getAll() {
        return repository.findAllActive();
    }

    @SuppressWarnings("java:S3864")
    @Override
    public Map<String, List<Object>> update(@NotNull List<CargoDeliveryTime> newData) {
        List<CargoDeliveryTime> oldDeliveryTimes = getAll();
        List<UUID> deletedIds = new ArrayList<>();
    
        List<CargoDeliveryTime> inactiveDeliveryTimes = new ArrayList<>();
    
        List<CargoDeliveryTime> newDeliveryTimes = newData.stream()
                                                          .peek(v -> {
                                                              Optional<CargoDeliveryTime> oldOptional =
                                                                      getOldChanged(v, oldDeliveryTimes);
                                                              if (oldOptional.isPresent()) {
                                                                  CargoDeliveryTime old = oldOptional.get();
                                                                  old.setActive(false);
                                                                  inactiveDeliveryTimes.add(old);
                                                                  deletedIds.add(old.getId());
                                                                  
                                                                  v.setId(null);
                                                              }
                                                          })
                                                          .collect(Collectors.toList());
        
        repository.saveAll(inactiveDeliveryTimes);
        
        List<CargoDeliveryTime> savedDeliveryTimes = repository.saveAll(newDeliveryTimes);
        
        var map = new HashMap<String, List<?>>(4);
        map.put(updatedKey(), deletedIds);
        map.put(savedKey(), savedDeliveryTimes);
        
        return ReflectionUtils.cast(map);
    }
    
    private Optional<CargoDeliveryTime> getOldChanged(CargoDeliveryTime cdt, List<CargoDeliveryTime> col) {
        return col.stream()
                  .filter(v -> v.getId().equals(cdt.getId()) && !v.getValue().equals(cdt.getValue()))
                  .findFirst();
    }
}
