package ru.sber.transport.tariff_fleet.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import ru.sber.transport.tariff_fleet.database.model.EwbTariff;
import ru.sber.transport.tariff_fleet.database.model.Tariff;
import ru.sber.transport.tariff_fleet.database.projection.GetEwbTariffByIdProjection;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbTariffGetByIdDto;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbTariffPostDto;
import ru.sber.transport.tariff_fleet.messaging.sender.message.EwbTariffMessage;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EwbTariffMapperTest {
    
    private final EwbTariffMapper ewbTariffMapper = Mappers.getMapper(EwbTariffMapper.class);
    
    @Test
    void ewbPostTariffDtoToEwbTariff() {
        var ewbPostTariffDto = Instancio.create(EwbTariffPostDto.class);
        var tariff = Instancio.create(Tariff.class);
        var organizationId = UUID.randomUUID();
        var expected = new EwbTariff(null,
                                     tariff,
                                     organizationId,
                                     ewbPostTariffDto.getDepartmentId(),
                                     ewbPostTariffDto.getAmount());
        var actual = ewbTariffMapper.ewbPostTariffDtoToEwbTariff(ewbPostTariffDto, tariff, organizationId);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        assertThat(ewbTariffMapper.ewbPostTariffDtoToEwbTariff(null, null, null)).isNull();
    }
    
    @Test
    void ewbTariffToEwbTariffMessage() {
        var source = Instancio.create(EwbTariff.class);
        var expected = new EwbTariffMessage(
                source.getTariffId(),
                source.getTariffId(),
                source.getTariff().getContractId(),
                source.getOrganizationId(),
                source.getDepartmentId(),
                source.getTariff().isActive());
        var actual = ewbTariffMapper.ewbTariffToEwbTariffMessage(source);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        assertThat(ewbTariffMapper.ewbTariffToEwbTariffMessage(null)).isNull();
    }
    
    @Test
    void getEwbTariffByIdProjectionToEwbTariffGetByIdDto() {
        var expected = Instancio.create(EwbTariffGetByIdDto.class);
        var projection = createGetEwbContractProjection(expected);
        var actual = ewbTariffMapper.getEwbTariffByIdProjectionToEwbTariffGetByIdDto(projection);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
    
    private GetEwbTariffByIdProjection createGetEwbContractProjection(EwbTariffGetByIdDto ewbTariffGetByIdDto) {
        var factory = new SpelAwareProxyProjectionFactory();
        var projection = factory.createProjection(GetEwbTariffByIdProjection.class);
        projection.setId(ewbTariffGetByIdDto.getId());
        projection.setInspectionType(ewbTariffGetByIdDto.getInspectionType());
        projection.setFleetOwnerName(ewbTariffGetByIdDto.getFleetOwner().name());
        projection.setFleetOwnerDepartmentName(ewbTariffGetByIdDto.getFleetOwner().departmentName());
        projection.setContractorName(ewbTariffGetByIdDto.getContractorName());
        projection.setContractNumber(ewbTariffGetByIdDto.getContractNumber());
        projection.setAmount(ewbTariffGetByIdDto.getAmount());
        return projection;
    }
}