package tech.isbt.tariff_dzo.dto.contract

import io.swagger.v3.oas.annotations.media.Schema
import tech.isbt.tariff_dzo.dto.common.ContractStatus
import java.time.LocalDate
import java.util.UUID

@Schema(description = "Фильтры поиска договоров")
data class ContractSearchRequest(
    @Schema(description = "Фильтр по активному статусу")
    val active: Boolean? = null,
    @Schema(description = "Идентификаторы организаций")
    val organizationIds: List<UUID>? = null,
    @Schema(description = "Идентификаторы контрагентов")
    val contractorId: List<UUID>? = null,
    @Schema(description = "Дата начала действия договора")
    val startDate: LocalDate? = null,
    @Schema(description = "Дата окончания действия договора")
    val endDate: LocalDate? = null,
    @Schema(description = "Статус договора")
    val status: ContractStatus? = null,
    @Schema(description = "Номер страницы", defaultValue = "0")
    val page: Int = 0,
    @Schema(description = "Размер страницы", defaultValue = "10")
    val size: Int = 10
)
