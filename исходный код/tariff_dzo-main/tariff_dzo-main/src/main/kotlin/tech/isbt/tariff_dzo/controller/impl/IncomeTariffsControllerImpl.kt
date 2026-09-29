package tech.isbt.tariff_dzo.controller.impl

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import tech.isbt.tariff_dzo.controller.IncomeTariffsController
import tech.isbt.tariff_dzo.dto.common.IncomeServiceType
import tech.isbt.tariff_dzo.dto.common.ServiceType
import tech.isbt.tariff_dzo.dto.income.IncomeTariffCreateRequest
import tech.isbt.tariff_dzo.dto.income.IncomeTariffDto
import tech.isbt.tariff_dzo.dto.income.IncomeTariffSearchRequest
import tech.isbt.tariff_dzo.dto.income.IncomeTariffUpdateRequest
import tech.isbt.tariff_dzo.mapper.IncomeTariffMapper
import java.util.UUID
import kotlin.math.min

@RestController
class IncomeTariffsControllerImpl(
    private val incomeTariffMapper: IncomeTariffMapper
) : IncomeTariffsController {

    override fun searchIncomeTariffs(request: IncomeTariffSearchRequest): Page<IncomeTariffDto> {
        val filtered = testIncomeTariffs.filter { it.matches(request) }
            .sortedBy { it.humanReadableId }

        val pageable = PageRequest.of(request.page, request.size)
        val totalElements = filtered.size
        val (fromIndex, toIndex) = calcIndexes(pageable.pageNumber, pageable.pageSize, totalElements)
        val content = filtered.subList(fromIndex, toIndex)
        return PageImpl(content, pageable, totalElements.toLong())
    }

    override fun createIncomeTariff(request: IncomeTariffCreateRequest) {
        testIncomeTariffs.add(incomeTariffMapper.toDto(request))
    }

    override fun getIncomeTariff(id: UUID): IncomeTariffDto {
        return testIncomeTariffs.find { it.id == id }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Доходный тариф с id=$id не найден")
    }

    override fun updateIncomeTariff(id: UUID, request: IncomeTariffUpdateRequest) {
        val index = testIncomeTariffs.indexOfFirst { it.id == id }
        if (index == -1) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Доходный тариф с id=$id не найден")
        }
        val existing = testIncomeTariffs[index]
        testIncomeTariffs[index] = existing.copy(
            hourNormalizedPrice = request.hourNormalizedPrice ?: existing.hourNormalizedPrice,
            avgStandartHour = request.avgStandartHour ?: existing.avgStandartHour,
            detailDiscountPrice = request.detailDiscountPrice ?: existing.detailDiscountPrice,
            avgPartsDiscountPercent = request.avgPartsDiscountPercent ?: existing.avgPartsDiscountPercent,
            workWarranty = request.workWarranty ?: existing.workWarranty,
            mileageWarranty = request.mileageWarranty ?: existing.mileageWarranty,
            detailWarranty = request.detailWarranty ?: existing.detailWarranty,
            fieldService = request.fieldService ?: existing.fieldService
        )
    }

    private fun IncomeTariffDto.matches(searchRequest: IncomeTariffSearchRequest): Boolean {
        return (searchRequest.service == null || service == searchRequest.service) &&
                (searchRequest.serviceType == null || serviceType == searchRequest.serviceType) &&
                (searchRequest.contractId == null || contractId == searchRequest.contractId)
    }

    private fun calcIndexes(pageNumber: Int, pageSize: Int, totalElements: Int): Pair<Int, Int> {
        val fromIndex = (pageNumber * pageSize).coerceIn(0, totalElements)
        val toIndex = min(fromIndex + pageSize, totalElements)
        return fromIndex to toIndex
    }

    companion object {
        private val testIncomeTariffs: MutableList<IncomeTariffDto> = (1..15).map { i ->
            IncomeTariffDto(
                id = UUID.randomUUID(),
                humanReadableId = "TF-0001-%08d".format(514 + i),
                service = if (i % 2 == 0) ServiceType.AUTOSERVICE else ServiceType.REPAIR_AND_MAINTENANCE,
                serviceType = IncomeServiceType.REPAIR_AND_MAINTENANCE,
                organizationName = "000001_Тестовая_организация_%02d".format(i),
                contractorName = "АвтоПилот%d".format(i),
                contractNumber = "459875642%02d".format(i),
                contractId = UUID.randomUUID(),
                hourNormalizedPrice = 84_000.0 + i * 100,
                avgStandartHour = 470.0 + i,
                detailDiscountPrice = 5.0,
                avgPartsDiscountPercent = 3.0,
                workWarranty = 12,
                mileageWarranty = 10_000,
                detailWarranty = 12,
                departmentName = "Тестовый_департамент_%02d".format(i),
                fieldService = i % 2 == 0,
                userId = UUID.randomUUID(),
                creationTime = System.currentTimeMillis() - i * 86_400_000L
            )
        }.toMutableList()
    }
}
