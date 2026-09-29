package ru.sber.transport.tariff_fleet.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbContractPostDto;
import ru.sber.transport.tariff_fleet.dto.fuel.FuelContractPostAllOrganizationsDto;
import ru.sber.transport.tariff_fleet.dto.repair.RepairContractPostSelfOrganizationDto;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ContractMapperTest {
    
    private final ContractMapper contractMapper = Mappers.getMapper(ContractMapper.class);
    
    @Test
    void abstractPostContractDtoToContract() {
        var contractDto = Instancio.create(EwbContractPostDto.class);
        var active = LocalDate.now().isBefore(contractDto.getPeriod().start()) && !LocalDate.now().isAfter(contractDto.getPeriod().end());
        var expected = new Contract(null,
                                    null,
                                    null,
                                    contractDto.getPeriod().start(),
                                    contractDto.getPeriod().end(),
                                    contractDto.getNumber(),
                                    contractDto.getUvhd(),
                                    active);
        var actual = contractMapper.abstractPostContractDtoToContract(contractDto, active);
        assertThat(actual).usingRecursiveComparison()
                          .isEqualTo(expected);
        assertThat(contractMapper.abstractPostContractDtoToContract(null, true)).isNull();
    }
    
    @Test
    void abstractContractPostAllOrganizationsDtoToContract() {
        var contractDto = Instancio.create(FuelContractPostAllOrganizationsDto.class);
        var active = LocalDate.now().isBefore(contractDto.getStart()) && !LocalDate.now().isAfter(contractDto.getEnd());
        var expected = new Contract(null,
                                    null,
                                    null,
                                    contractDto.getStart(),
                                    contractDto.getEnd(),
                                    contractDto.getNumber(),
                                    contractDto.getUvhd(),
                                    active);
        var actual = contractMapper.abstractContractPostAllOrganizationsDtoToContract(contractDto, active);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }
    
    @Test
    void abstractContractPostSelfOrganizationDtoToContract() {
        var contractDto = Instancio.create(RepairContractPostSelfOrganizationDto.class);
        var active = LocalDate.now().isBefore(contractDto.getStart()) && !LocalDate.now().isAfter(contractDto.getEnd());
        var expected = new Contract(null,
                                    null,
                                    null,
                                    contractDto.getStart(),
                                    contractDto.getEnd(),
                                    contractDto.getNumber(),
                                    contractDto.getUvhd(),
                                    active);
        var actual = contractMapper.abstractContractPostSelfOrganizationDtoToContract(contractDto, active);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }
}