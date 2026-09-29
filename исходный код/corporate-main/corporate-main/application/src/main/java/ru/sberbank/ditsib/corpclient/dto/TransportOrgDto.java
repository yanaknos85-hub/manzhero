package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

@Setter
@Getter
@Schema(title = "Информация об активности вида транспорта", description = "Информация об активности вида транспорта")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TransportOrgDto {
    /**
     * ID.
     */
    @Schema(description = "Вид транспорта")
    private String transportType;

    /**
     * Name of attribute
     */
    @Schema(description = "Активный")
    private boolean active;
}
