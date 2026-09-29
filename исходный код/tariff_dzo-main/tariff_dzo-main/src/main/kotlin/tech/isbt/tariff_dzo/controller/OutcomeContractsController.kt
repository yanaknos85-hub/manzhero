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
import tech.isbt.tariff_dzo.dto.contract.ContractOutcomeCreateRequest
import tech.isbt.tariff_dzo.dto.contract.ContractSearchRequest
import tech.isbt.tariff_dzo.dto.contract.ContractUpdateRequest
import java.util.*

@RequestMapping("api/v1/tariff-repair/contracts")
@Tag(name = "Расходные договоры:WIP", description = "Договоры расходные:WIP(черновой вариант)")
interface OutcomeContractsController {

    @Operation(
        summary = "Получить список расходных договоров",
        description = "Получение списка расходных договоров с постраничной выгрузкой"
    )
    @PostMapping("/outcome/search")
    fun searchOutcomeContracts(@RequestBody request: ContractSearchRequest): Page<ContractDto>

    @Operation(summary = "Создать расходный договор", description = "Добавление нового расходный договора")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/outcome")
    fun createOutComeContract(@RequestBody request: ContractOutcomeCreateRequest)

    @Operation(summary = "Получить расходный договор", description = "Получение информации о договоре по идентификатору")
    @GetMapping("/outcome/{id}")
    fun getContract(@PathVariable id: UUID): ContractDto

    @Operation(
        summary = "Обновить расходный договор",
        description = "Частичное обновление договора (возможно изменение одного или нескольких полей)"
    )
    @PatchMapping("/outcome/{id}")
    fun updateContract(@PathVariable id: UUID, @RequestBody request: ContractUpdateRequest)

    @Operation(summary = "Удалить расходный договор", description = "Удаление договора по идентификатору")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/outcome/{id}")
    fun deleteContract(@PathVariable id: UUID)
}
