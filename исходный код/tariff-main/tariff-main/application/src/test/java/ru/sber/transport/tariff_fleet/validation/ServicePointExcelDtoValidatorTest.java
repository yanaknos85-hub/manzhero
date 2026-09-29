package ru.sber.transport.tariff_fleet.validation;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointExcelDto;
import ru.sber.transport.tariff_fleet.dto.service_point.ValidatedServicePointDto;
import ru.sber.transport.tariff_fleet.mapper.ServicePointMapper;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ServicePointExcelDtoValidatorTest {
    
    public static final String ERROR_MSG = "Не заполнены поля: [Номер по порядку, Адрес точки обслуживания, Широта, Долгота]";
    @InjectMocks
    private ServicePointExcelDtoValidator validator;
    @Mock
    private ServicePointMapper mapper;
    
    @Test
    void validateAndMap() {
        doReturn(new ValidatedServicePointDto(null, null, null, null, ERROR_MSG))
                .when(mapper).servicePointExcelDtoToValidatedServicePointDto(any(ServicePointExcelDto.class), eq(ERROR_MSG));
        var nullEntry = Instancio.of(ServicePointExcelDto.class)
                                 .set(field(ServicePointExcelDto::getNumber), null)
                                 .set(field(ServicePointExcelDto::getAddress), null)
                                 .set(field(ServicePointExcelDto::getLatitude), null)
                                 .set(field(ServicePointExcelDto::getLongitude), null)
                                 .create();
        var actualResult = validator.validateAndMap(List.of(nullEntry));
        
        assertThat(actualResult).satisfies(res -> {
            assertTrue(res.hasAnyError());
            var dto = res.enrtyList().get(0);
            assertNull(dto.number());
            assertNull(dto.address());
            assertNull(dto.latitude());
            assertNull(dto.longitude());
            assertEquals(ERROR_MSG, dto.error());
        });
    }
    
}
