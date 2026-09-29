package tech.isbt.tariff_dzo.dto.contract

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Данные для обновления договора")
data class ContractUpdateRequest(
    @Schema(description = "Номер договора")
    val contractNumber: String? = null,
    @Schema(description = "Сумма без НДС")
    val amountWithoutVat: Long? = null,
    @Schema(description = "Наименование точки")
    val pointName: String? = null,
    @Schema(description = "Логотип в формате base64")
    val logo: String? = null
)
