package ru.sber.transport.tariff_fleet.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointExcelDto;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тест маппера точки обслуживания")
class ServicePointMapperTest {
    
    private final ServicePointMapper mapper = Mappers.getMapper(ServicePointMapper.class);
    
    @Test
    void servicePointExcelDtoToValidatedServicePointDto() {
        var expectedServicePointExcelDto = Instancio.create(ServicePointExcelDto.class);
        var expectedError = Instancio.create(String.class);
        var actual = mapper.servicePointExcelDtoToValidatedServicePointDto(expectedServicePointExcelDto, expectedError);
        
        assertThat(actual.number()).isEqualTo(expectedServicePointExcelDto.getNumber());
        assertThat(actual.address()).isEqualTo(expectedServicePointExcelDto.getAddress());
        assertThat(actual.latitude()).isEqualTo(expectedServicePointExcelDto.getLatitude());
        assertThat(actual.longitude()).isEqualTo(expectedServicePointExcelDto.getLongitude());
        assertThat(actual.error()).isEqualTo(expectedError);
    }
}
