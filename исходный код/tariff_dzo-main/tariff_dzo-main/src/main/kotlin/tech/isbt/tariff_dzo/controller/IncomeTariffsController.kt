package tech.isbt.tariff_dzo.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import tech.isbt.tariff_dzo.dto.income.IncomeTariffCreateRequest
import tech.isbt.tariff_dzo.dto.income.IncomeTariffDto
import tech.isbt.tariff_dzo.dto.income.IncomeTariffSearchRequest
import tech.isbt.tariff_dzo.dto.income.IncomeTariffUpdateRequest
import java.util.*

@RequestMapping("api/v1/tariff-repair")
@Tag(name = "Доходные тарифы", description = "API для работы с доходными тарифами")
interface IncomeTariffsController {

    @Operation(
        summary = "Получить список доходных тарифов",
        description = "Получение списка доходных тарифов с постраничной выгрузкой"
    )
    @PostMapping("/income/search")
    fun searchIncomeTariffs(@RequestBody request: IncomeTariffSearchRequest): Page<IncomeTariffDto>

    @Operation(summary = "Создать доходный тариф", description = "Добавление нового доходного тарифа")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/income")
    fun createIncomeTariff(@RequestBody request: IncomeTariffCreateRequest)

    @Operation(
        summary = "Получить доходный тариф",
        description = "Получение информации о доходном тарифе по идентификатору"
    )
    @GetMapping("/income/{id}")
    fun getIncomeTariff(@PathVariable id: UUID): IncomeTariffDto

    @Operation(
        summary = "Обновить доходный тариф",
        description = "Частичное обновление доходного тарифа (возможно обновление одного или нескольких полей)"
    )
    @PatchMapping("/income/{id}")
    fun updateIncomeTariff(@PathVariable id: UUID, @RequestBody request: IncomeTariffUpdateRequest)
}