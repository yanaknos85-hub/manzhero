package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;
import ru.sberbank.ditsib.corpclient.dto.docs.DocumentsDTO;
import ru.sberbank.ditsib.transport.constants.PersonalCarOwnerInfo;
import ru.sberbank.ditsib.transport.constants.PersonalTransportType;

import java.util.UUID;

/**
 * Data transfer object with data about new personal car.
 */
@Getter
@Setter
@SuperBuilder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(title = "Новые данные о личном автомобиле", description = "Данные персонального автомобиля")
public class NewPersonalCarDTO {
    
    /**
     * Bran name
     */
    @Schema(description = "Марка")
    private String brandName;
    
    /**
     * Model name
     */
    @Schema(description = "Модель")
    private String model;
    
    @Length(max = 30)
    @Builder.Default
    @Schema(description = "Цвет", defaultValue = "Не указан", maxLength = 30)
    private String color = "Не указан";
    
    @Builder.Default
    @Schema(description = "Тип транспорта (автомобиль/мотоцикл)", defaultValue = "CAR")
    private PersonalTransportType transportType = PersonalTransportType.CAR;
    
    /**
     * Registration number
     */
    @NotBlank
    @Length(min = 7, max = 20)
    @Schema(description = "Гос. номер", minLength = 7, maxLength = 20)
    private String registrationNumber;
    
    /**
     * registration certificate
     */
    @Schema(description = "Номер свидетельства о регистрации")
    private String registrationCertificate;

    /**
     * Insurance number
     */
    @Schema(description = "Номер ОСАГО")
    private String insuranceNumber;
    
    /**
     * Corporate user
     */
    @JsonIgnore
    private UUID employeeId;

    @Min(0)
    @Max(40)
    @Builder.Default
    @Schema(description = "Количество пассажирских мест", defaultValue = "0", minimum = "0", maximum = "40")
    private short passengerSeatsCount = 0;
    
    /**
     * Owner info
     */
    @Schema(description = "Информация о владельце")
    private PersonalCarOwnerInfo ownerInfo;

    /**
     * User's accept on personal data processing sign
     */
    @Schema(description = "Признак согласия на обработку ПД: true - согласие получено, false - согласие не получено")
    private boolean persDataAccept;

    @Schema(description = "Пакет документов для компенсации затрат на использование личного транспорта")
    private DocumentsDTO documents;
}
