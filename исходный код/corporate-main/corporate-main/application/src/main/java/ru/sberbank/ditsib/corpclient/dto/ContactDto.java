package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.sberbank.ditsib.corpclient.validation.annotation.ContactValidation;

import jakarta.validation.constraints.NotNull;

/**
 * Объект данных контакта.
 */
@Getter
@Setter
@Schema(title = "Контактные данные", description = "Контактные данные")
@ContactValidation
@ToString
public class ContactDto {
    
    @NotNull
    @Schema(description = "Тип контактных данных")
    private ContactType type;
    
    @NotNull
    @Schema(description = "Значение контакта")
    private String value;
    
}
