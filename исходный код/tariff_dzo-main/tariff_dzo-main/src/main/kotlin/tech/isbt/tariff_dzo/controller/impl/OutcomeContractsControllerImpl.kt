package tech.isbt.tariff_dzo.controller.impl

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import tech.isbt.tariff_dzo.controller.OutcomeContractsController
import tech.isbt.tariff_dzo.dto.common.ContractStatus
import tech.isbt.tariff_dzo.dto.common.ContractType
import tech.isbt.tariff_dzo.dto.common.RestrictionType
import tech.isbt.tariff_dzo.dto.common.ServiceType
import tech.isbt.tariff_dzo.dto.contract.ContractDto
import tech.isbt.tariff_dzo.dto.contract.ContractOutcomeCreateRequest
import tech.isbt.tariff_dzo.dto.contract.ContractSearchRequest
import tech.isbt.tariff_dzo.dto.contract.ContractUpdateRequest
import tech.isbt.tariff_dzo.mapper.ContractMapper
import java.time.LocalDate
import java.util.UUID
import kotlin.math.min

@RestController
class OutcomeContractsControllerImpl(
    private val contractMapper: ContractMapper
) : OutcomeContractsController {

    override fun searchOutcomeContracts(request: ContractSearchRequest): Page<ContractDto> {
        val filtered = testOutcomeContracts.filter { it.matches(request) }
            .sortedBy { it.contractNumber }

        val pageable = PageRequest.of(request.page, request.size)
        val totalElements = filtered.size
        val (fromIndex, toIndex) = calcIndexes(pageable.pageNumber, pageable.pageSize, totalElements)
        val content = filtered.subList(fromIndex, toIndex)
        return PageImpl(content, pageable, totalElements.toLong())
    }

    override fun createOutComeContract(request: ContractOutcomeCreateRequest) {
        testOutcomeContracts.add(contractMapper.toDto(request, ContractType.OUTCOME))
    }

    override fun getContract(id: UUID): ContractDto {
        return testOutcomeContracts.find { it.id == id }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Расходный договор с id=$id не найден")
    }

    override fun updateContract(id: UUID, request: ContractUpdateRequest) {
        val index = testOutcomeContracts.indexOfFirst { it.id == id }
        if (index == -1) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Расходный договор с id=$id не найден")
        }
        val existing = testOutcomeContracts[index]
        testOutcomeContracts[index] = existing.copy(
            contractNumber = request.contractNumber ?: existing.contractNumber,
            amountWithoutVat = request.amountWithoutVat ?: existing.amountWithoutVat
        )
    }

    override fun deleteContract(id: UUID) {
        val removed = testOutcomeContracts.removeIf { it.id == id }
        if (!removed) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Расходный договор с id=$id не найден")
        }
    }

    private fun ContractDto.matches(searchRequest: ContractSearchRequest): Boolean {
        val contractorIds = searchRequest.contractorId
        val organizationIds = searchRequest.organizationIds
        return (searchRequest.active == null || active == searchRequest.active) &&
                (searchRequest.status == null || active == (searchRequest.status == ContractStatus.ACTIVE)) &&
                (organizationIds.isNullOrEmpty() || this.organizationIds.any { it in organizationIds }) &&
                (contractorIds.isNullOrEmpty() || contractorId in contractorIds) &&
                (searchRequest.startDate == null || !startDate.isBefore(searchRequest.startDate)) &&
                (searchRequest.endDate == null || !endDate.isAfter(searchRequest.endDate))
    }

    private fun calcIndexes(pageNumber: Int, pageSize: Int, totalElements: Int): Pair<Int, Int> {
        val fromIndex = (pageNumber * pageSize).coerceIn(0, totalElements)
        val toIndex = min(fromIndex + pageSize, totalElements)
        return fromIndex to toIndex
    }

    companion object {
        private val testOutcomeContracts: MutableList<ContractDto> = (1..15).map { i ->
            ContractDto(
                id = UUID.randomUUID(),
                contractorId = UUID.randomUUID(),
                organizationIds = listOf(UUID.randomUUID()),
                organizationNames = listOf("000001_Тестовая_организация_%02d".format(i)),
                region = "Москва и Московская область, Москва",
                serviceType = if (i % 2 == 0) ServiceType.REPAIR_AND_MAINTENANCE else ServiceType.AUTOSERVICE,
                amountWithoutVat = 100_000L + i * 1_000L,
                userId = UUID.randomUUID(),
                creationTime = System.currentTimeMillis() - i * 86_400_000L,
                startDate = LocalDate.now().minusMonths(i.toLong()),
                endDate = LocalDate.now().plusYears(1),
                active = i % 3 != 0,
                tariffsExist = i % 2 == 0,
                contractNumber = "%05d".format(82_500 + i),
                vatValue = 10,
                regionIds = listOf(UUID.randomUUID(), UUID.randomUUID()),
                settings = emptyList(),
                contractType = ContractType.OUTCOME,
                restrictionType = RestrictionType.NONE,
                restrictedIds = emptyList()
            )
        }.toMutableList()
    }
}
