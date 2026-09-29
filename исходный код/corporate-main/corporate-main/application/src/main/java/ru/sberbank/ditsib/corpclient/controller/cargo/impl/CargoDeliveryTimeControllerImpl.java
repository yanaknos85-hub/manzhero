package ru.sberbank.ditsib.corpclient.controller.cargo.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.corpclient.controller.cargo.CargoDeliveryTimeController;
import ru.sberbank.ditsib.corpclient.database.model.CargoDeliveryTime;
import ru.sberbank.ditsib.corpclient.dto.cargo.CargoDeliveryTimeDto;
import ru.sberbank.ditsib.corpclient.dto.mapper.CargoDeliveryTimeMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.CargoDeliveryTimeSender;
import ru.sberbank.ditsib.corpclient.service.cargo.CargoDeliveryTimeService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
class CargoDeliveryTimeControllerImpl implements CargoDeliveryTimeController {
    private final CargoDeliveryTimeService service;
    private final CargoDeliveryTimeSender sender;
    private final CargoDeliveryTimeMapper mapper;

    @SuppressWarnings("java:S3958")
    @Override
    public Collection<CargoDeliveryTimeDto> getAll() {
        return service.getAll().stream()
                      .map(mapper::entityToDto)
                      .toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public Collection<CargoDeliveryTimeDto> update(List<CargoDeliveryTimeDto> data) {
        List<CargoDeliveryTime> deliveryTimes = data.stream()
                                                    .map(mapper::dtoToEntity)
                                                    .toList();
        
        var map = service.update(deliveryTimes);
    
        map.get(service.updatedKey())
           .stream()
           .map(UUID.class::cast)
           .forEach(sender::sendDeleted);
    
        map.get(service.savedKey())
           .stream()
           .map(CargoDeliveryTime.class::cast)
           .forEach(sender::send);
        
        return getAll();
    }
}
