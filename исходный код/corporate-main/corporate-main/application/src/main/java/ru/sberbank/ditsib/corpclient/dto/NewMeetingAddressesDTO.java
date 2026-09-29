package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Setter
@Getter
@Schema(title = "Информация о новом адресе", description = "Данные нового адреса")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class NewMeetingAddressesDTO {

    /**
     * label
     */
    @Schema(description = "Метка", maxLength = 255)
    @NotBlank
    @Length(max = 255)
    private String label;

    /**
     * Address
     */
    @Schema(description = "Адрес")
    @NotNull
    private @Valid NewAddressDto address;
}
