package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Объект обмена данными об организациях.
 */
@Getter
@Setter
@ToString
@Schema(title = "Новые данные об организации", description = "Данные организации")
public class NewOrganizationDTO {
    
    /**
     * Юр. название организации.
     */
    @NotBlank
    @Schema(description = "Юридическое наименование")
    private String officialName;
    
    /**
     * Юр. адрес.
     */
    @NotBlank
    @Schema(description = "Адрес")
    private String address;
    
    /**
     * Список контактов.
     */
    @Schema(description = "Список контактов")
    private List<@Valid ContactDto> contacts = new ArrayList<>();
    
    /**
     * ОГРН.
     */
    @Schema(description = "ОГРН")
    private String msrn;

    /**
     * Код организационной единицы.
     */
    @Schema(description = "organization_code")
    private Integer organizationCode;

    /**
     * ИНН.
     */
    @Schema(description = "ИНН", minLength = 10, maxLength = 12)
    @Size(min = 10, max = 12)
    private String tid;
    
    
    @Schema(description = "Идентификатор ЕАСУП, для внутрибанковских структур поле обязательное")
     private String easupId;

    @Schema(description = "Группа организаций")
    private OrganizationGroupResponseDTO organizationGroup;
}
