package tech.isbt.tariff_dzo.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import tech.isbt.tariff_dzo.dto.contract.ContractDto
import tech.isbt.tariff_dzo.dto.contract.ContractIncomeCreateRequest
import tech.isbt.tariff_dzo.dto.contract.ContractSearchRequest
import tech.isbt.tariff_dzo.dto.contract.ContractUpdateRequest
import java.util.*

@RequestMapping("api/v1/tariff-repair/contracts")
@Tag(name = "Доходные договоры:WIP", description = "Договоры доходные:WIP(черновой вариант)")
interface IncomeContractsController {

    @Operation(
        summary = "Получить список доходных договоров",
        description = "Получение списка расходных договоров с постраничной выгрузкой"
    )
    @PostMapping("/income/search")
    fun searchIncomeContracts(@RequestBody request: ContractSearchRequest): Page<ContractDto>

    @Operation(summary = "Создать доходный договор", description = "Добавление нового доходного договора")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/income")
    fun createIncome(@RequestBody request: ContractIncomeCreateRequest)

    @Operation(
        summary = "Получить доходный договор",
        description = "Получение информации о договоре по идентификатору"
    )
    @GetMapping("/income/{id}")
    fun getContract(@PathVariable id: UUID): ContractDto

    @Operation(
        summary = "Обновить доходный договор",
        description = "Частичное обновление договора (возможно изменение одного или нескольких полей)"
    )
    @PatchMapping("/income/{id}")
    fun updateContract(@PathVariable id: UUID, @RequestBody request: ContractUpdateRequest)

    @Operation(summary = "Удалить доходный договор", description = "Удаление договора по идентификатору")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/income/{id}")
    fun deleteContract(@PathVariable id: UUID)
}
