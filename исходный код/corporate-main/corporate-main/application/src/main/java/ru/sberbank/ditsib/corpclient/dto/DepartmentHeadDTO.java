package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(title = "Информация о главе департамента", description = "Данные главы департамента")
public class DepartmentHeadDTO {
    
    /**
     * Head of department
     */
    @Schema(description = "Идентификатор главы подразделения")
    private UUID id;
    
    /**
     * First name
     */
    @Schema(description = "Имя", maxLength = 20)
    private String firstName;
    
    /**
     * Last name
     */
    @Schema(description = "Фамилия", maxLength = 30)
    private String lastName;
    
    /**
     * Patronymic
     */
    @Schema(description = "Отчество", maxLength = 30)
    private String patronymic;
    
}
