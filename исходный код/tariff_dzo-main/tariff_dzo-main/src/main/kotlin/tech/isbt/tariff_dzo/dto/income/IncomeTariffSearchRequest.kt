package tech.isbt.tariff_dzo.dto.income

import io.swagger.v3.oas.annotations.media.Schema
import tech.isbt.tariff_dzo.dto.common.IncomeServiceType
import tech.isbt.tariff_dzo.dto.common.ServiceType
import java.util.UUID

@Schema(description = "Фильтры поиска доходных тарифов")
data class IncomeTariffSearchRequest(
    @Schema(description = "Тип сервиса")
    val service: ServiceType? = null,
    @Schema(description = "Тип услуги")
    val serviceType: IncomeServiceType? = null,
    @Schema(description = "Идентификатор клиента")
    val clientId: UUID? = null,
    @Schema(description = "Фильтр по активному статусу (строковое значение \"true\"/\"false\")")
    val active: String? = null,
    @Schema(description = "Идентификатор договора")
    val contractId: UUID? = null,
    @Schema(description = "Номер страницы", defaultValue = "0")
    val page: Int = 0,
    @Schema(description = "Размер страницы", defaultValue = "10")
    val size: Int = 10
)
