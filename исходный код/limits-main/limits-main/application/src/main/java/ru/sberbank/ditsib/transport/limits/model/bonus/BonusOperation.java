package ru.sberbank.ditsib.transport.limits.model.bonus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Операции с бонусным счётом")
public enum BonusOperation {
    @Schema(description = "Пополнение")
    DEPOSIT,
    @Schema(description = "Снятие")
    SPEND
}
