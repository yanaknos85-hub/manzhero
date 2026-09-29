package ru.sberbank.ditsib.transport.limits.model.bonus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Статус операции с бонусным счётом")
public enum BonusRequestStatus {
    @Schema(description = "Сумма зарезервирована")
    RESERVED,
    @Schema(description = "Операция успешно выполнена")
    DONE,
    @Schema(description = "Операция отменена")
    CANCELED
}
