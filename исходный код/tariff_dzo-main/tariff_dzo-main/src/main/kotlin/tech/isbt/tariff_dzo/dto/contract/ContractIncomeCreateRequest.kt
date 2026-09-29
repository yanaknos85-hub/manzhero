package tech.isbt.tariff_dzo.dto.contract

import io.swagger.v3.oas.annotations.media.Schema
import tech.isbt.tariff_dzo.dto.common.ContractType
import tech.isbt.tariff_dzo.dto.common.RestrictionType
import tech.isbt.tariff_dzo.dto.common.ServiceType
import java.time.LocalDate
import java.util.UUID

@Schema(description = "Данные для создания доходного договора")
data class ContractIncomeCreateRequest(
    @Schema(description = "Идентификаторы организаций")
    val organizationIds: List<UUID>,
    @Schema(description = "Тип сервиса")
    val serviceType: ServiceType,
    @Schema(description = "Номер договора")
    val contractNumber: String,
    @Schema(description = "Сумма без НДС (только целое число, максимум 9 знаков, минимум 1 знак)")
    val amountWithoutVat: Long,
    @Schema(description = "Дата начала действия договора")
    val startDate: LocalDate,
    @Schema(description = "Дата окончания действия договора")
    val endDate: LocalDate,
    @Schema(description = "Значение НДС")
    val vatValue: Int,
    @Schema(description = "Тип ограничения по договору")
    val restrictionType: RestrictionType,
    @Schema(description = "Ограниченные идентификаторы (белый/чёрный список)")
    val restrictedIds: List<UUID>? = null,
//    @Schema(description = "Тип договора")
//    val contractType: ContractType,
    @Schema(description = "Наименование точки")
    val pointName: String? = null,
    @Schema(description = "Логотип в формате base64")
    val logo: String? = null
)
