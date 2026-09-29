package ru.sberbank.ditsib.corpclient.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import ru.sberbank.ditsib.corpclient.database.model.DocumentType;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(title = "Информация о ПТС", description = "Данные ПТС")
@Builder(toBuilder = true)
public record PassportTsDTO(
        @Schema(description = "Идентификатор")
        UUID id,

        @Schema(description = "Идентификатор сотрудника")
        UUID employeeId,

        @Schema(description = "Идентификатор транспортного средства")
        UUID carId,

        @Schema(description = "Имя файла")
        String fileName,

        @Schema(description = "Размер файла")
        Integer fileSize,

        @Schema(description = "Формат файла")
        String fileFormat,

        @Schema(description = "Цвет транспортного средства")
        String color,

        @Schema(description = "Количество пассажирских мест")
        Integer passengerSeatsCount,

        @Schema(description = "VIN транспортного средства")
        String vin,

        @Schema(description = "Объем двигателя")
        Integer engineVolume,

        @Schema(description = "Мощность двигателя")
        String enginePower,

        @Schema(description = "Марка")
        String brandName,

        @Schema(description = "Модель")
        String model
) {
}
