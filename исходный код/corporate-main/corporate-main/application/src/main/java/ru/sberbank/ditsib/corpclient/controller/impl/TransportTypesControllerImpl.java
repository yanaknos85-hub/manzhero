package ru.sberbank.ditsib.corpclient.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.corpclient.controller.TransportTypesController;
import ru.sberbank.ditsib.corpclient.dto.constant.TransportTypeEnumDTO;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.Arrays;
import java.util.List;

/**
 * Имплементация TransportTypesController
 */
@RequiredArgsConstructor
@RestController
public class TransportTypesControllerImpl implements TransportTypesController {
    @SuppressWarnings("java:S3958")
    @Override
    public List<TransportTypeEnumDTO> getAll() {
        return Arrays.stream(TransportTypeEnum.values())
                     .map(elt -> TransportTypeEnumDTO.builder()
                                                     .id(elt.getId())
                                                     .name(elt.getName())
                                                     .rusName(elt.getRusName())
                                                     .build())
                     .toList();
    }
}
