package ru.sber.transport.tariff_fleet.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.tariff_fleet.constant.ContractorType;
import ru.sber.transport.tariff_fleet.database.model.Contractor;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.instancio.Select.field;

class ContractorMapperTest {
    
    private final ContractorMapper contractorMapper = Mappers.getMapper(ContractorMapper.class);
    
    @Test
    void contractorToContractorDto() {
        var expected = Instancio.of(Contractor.class)
                                .set(field(Contractor::getContractorType), ContractorType.API)
                                .create();
        var actual = contractorMapper.contractorToContractorDto(expected);
        assertThat(actual.id()).isEqualTo(expected.getId());
        assertThat(actual.name()).isEqualTo(expected.getName());
        assertThat(actual.integrationType()).isEqualTo(expected.getContractorType().name());
    }
}
