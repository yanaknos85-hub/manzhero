package tech.isbt.tariff_dzo.dto.income

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Данные для обновления доходного тарифа")
data class IncomeTariffUpdateRequest(
    @Schema(description = "Ставка нормо-часа в копейках")
    val hourNormalizedPrice: Double? = null,
    @Schema(description = "Средняя ставка нормо-часа в копейках")
    val avgStandartHour: Double? = null,
    @Schema(description = "Скидка на запчасти в процентах")
    val detailDiscountPrice: Double? = null,
    @Schema(description = "Средняя скидка на запчасти в процентах")
    val avgPartsDiscountPercent: Double? = null,
    @Schema(description = "Гарантийный срок на работы по времени, месяца")
    val workWarranty: Int? = null,
    @Schema(description = "Гарантийный срок на работы по пробегу, км")
    val mileageWarranty: Int? = null,
    @Schema(description = "Гарантийный срок на запчасти, месяца")
    val detailWarranty: Int? = null,
    @Schema(description = "Флаг выездного обслуживания")
    val fieldService: Boolean? = null
)
