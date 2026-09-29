package tech.isbt.tariff_dzo.mapper

import org.mapstruct.Mapper
import tech.isbt.tariff_dzo.dto.income.IncomeTariffCreateRequest
import tech.isbt.tariff_dzo.dto.income.IncomeTariffDto

@Mapper(componentModel = "spring")
interface IncomeTariffMapper {
    fun toDto(request: IncomeTariffCreateRequest): IncomeTariffDto
}
