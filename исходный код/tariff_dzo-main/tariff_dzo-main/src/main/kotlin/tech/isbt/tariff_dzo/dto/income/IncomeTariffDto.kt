package tech.isbt.tariff_dzo.dto.income

import io.swagger.v3.oas.annotations.media.Schema
import tech.isbt.tariff_dzo.dto.common.IncomeServiceType
import tech.isbt.tariff_dzo.dto.common.ServiceType
import java.util.UUID

@Schema(description = "Доходный тариф")
data class IncomeTariffDto(
    @Schema(description = "Идентификатор тарифа")
    val id: UUID,
    @Schema(description = "Человекочитаемый идентификатор тарифа (формат TF-XXXX-XXXXXX)")
    val humanReadableId: String,
    @Schema(description = "Тип сервиса")
    val service: ServiceType,
    @Schema(description = "Тип услуги")
    val serviceType: IncomeServiceType,
    @Schema(description = "Наименование организации")
    val organizationName: String,
    @Schema(description = "Имя контрагента")
    val contractorName: String,
    @Schema(description = "Номер договора")
    val contractNumber: String,
    @Schema(description = "Идентификатор договора")
    val contractId: UUID,
    @Schema(description = "Ставка нормо-часа в копейках")
    val hourNormalizedPrice: Double,
    @Schema(description = "Средняя ставка нормо-часа в копейках")
    val avgStandartHour: Double,
    @Schema(description = "Скидка на запчасти в процентах")
    val detailDiscountPrice: Double,
    @Schema(description = "Средняя скидка на запчасти в процентах")
    val avgPartsDiscountPercent: Double,
    @Schema(description = "Гарантийный срок на работы по времени, месяца")
    val workWarranty: Int,
    @Schema(description = "Гарантийный срок на работы по пробегу, км")
    val mileageWarranty: Int,
    @Schema(description = "Гарантийный срок на запчасти, месяца")
    val detailWarranty: Int,
    @Schema(description = "Наименование департамента")
    val departmentName: String,
    @Schema(description = "Флаг выездного обслуживания")
    val fieldService: Boolean,
    @Schema(description = "Идентификатор пользователя")
    val userId: UUID,
    @Schema(description = "Время создания (epoch timestamp)")
    val creationTime: Long
)
