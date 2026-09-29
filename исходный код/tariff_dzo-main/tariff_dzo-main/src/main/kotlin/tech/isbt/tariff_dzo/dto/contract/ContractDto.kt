package tech.isbt.tariff_dzo.dto.contract

import io.swagger.v3.oas.annotations.media.Schema
import tech.isbt.tariff_dzo.dto.common.ContractType
import tech.isbt.tariff_dzo.dto.common.RestrictionType
import tech.isbt.tariff_dzo.dto.common.ServiceType
import java.time.LocalDate
import java.util.UUID

@Schema(description = "Договор")
data class ContractDto(
    @Schema(description = "Идентификатор договора")
    val id: UUID,
    @Schema(description = "Идентификатор контрагента")
    val contractorId: UUID,
    @Schema(description = "Идентификаторы организаций")
    val organizationIds: List<UUID>,
    @Schema(description = "Названия организаций")
    val organizationNames: List<String>,
    @Schema(description = "Регион")
    val region: String,
    @Schema(description = "Тип сервиса")
    val serviceType: ServiceType,
    @Schema(description = "Сумма без НДС")
    val amountWithoutVat: Long,
    @Schema(description = "Идентификатор пользователя")
    val userId: UUID,
    @Schema(description = "Время создания (epoch timestamp)")
    val creationTime: Long,
    @Schema(description = "Дата начала действия договора")
    val startDate: LocalDate,
    @Schema(description = "Дата окончания действия договора")
    val endDate: LocalDate,
    @Schema(description = "Активный ли договор")
    val active: Boolean,
    @Schema(description = "Существуют ли тарифы")
    val tariffsExist: Boolean,
    @Schema(description = "Номер договора")
    val contractNumber: String,
    @Schema(description = "Значение НДС")
    val vatValue: Int,
    @Schema(description = "Идентификаторы регионов")
    val regionIds: List<UUID>,
    @Schema(description = "Настройки")
    val settings: List<Any>? = null,
    @Schema(description = "Тип договора")
    val contractType: ContractType,
    @Schema(description = "Тип ограничения")
    val restrictionType: RestrictionType,
    @Schema(description = "Ограниченные идентификаторы (белый/чёрный список)")
    val restrictedIds: List<UUID>? = null
)
