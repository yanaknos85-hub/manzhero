package ru.sber.transport.tariff_fleet.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.tariff_fleet.constant.ActivationType;
import ru.sber.transport.tariff_fleet.database.model.Tariff;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbTariffPostDto;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class TariffMapperTest {
    
    private final TariffMapper tariffMapper = Mappers.getMapper(TariffMapper.class);
    
    @Test
    void abstractPostContractDtoToContract() {
        var contractId = UUID.randomUUID();
        var postTariffDto = Instancio.of(EwbTariffPostDto.class)
                .set(field(EwbTariffPostDto::getContractId),contractId)
                .create();
        var humanReadableId = "TF-0008-00000002";
        var expected = new Tariff(null,
                                  contractId,
                                  true,
                                  ActivationType.AUTO,
                                  humanReadableId,
                                  null,
                                  null);
        var actual = tariffMapper.abstractPostTariffDtoToTariff(postTariffDto, humanReadableId, true);
        assertThat(actual).usingRecursiveComparison()
                          .isEqualTo(expected);
        assertThat(tariffMapper.abstractPostTariffDtoToTariff(null, null, false)).isNull();
    }
}