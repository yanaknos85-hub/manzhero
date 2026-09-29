package ru.sberbank.ditsib.corpclient.database.model.docs;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.sberbank.ditsib.corpclient.database.model.DocumentType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "corporate", name = "employee_document")
@Data
public class EmployeeDocument {

    /**
     * Идентификатор
    */
    @Id
    private UUID id;

    /**
     * Код документа
     */
    @ManyToOne
    @JoinColumn(name = "document_code", nullable = false)
    @NotNull(message = "Set document type")
    private DocumentType documentType;

    /**
     * Идентификатор сотрудника
     */
    @Column(name = "employee_id")
    private UUID employeeId;

    /**
     * Идентификатор транспортного средства
     */
    @Column(name = "car_id")
    private UUID carId;

    /**
     * Имя файла
     */
    @Column(name = "file_name")
    private String fileName;

    /**
     * Размер файла
     */
    @Column(name = "file_size")
    private Integer fileSize;

    /**
     * Формат файла
     */
    @Column(name = "file_format")
    private String fileFormat;

    /**
     * Дата и время создания записи
     */
    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    /**
     * Пользователь, создавший запись
     */
    @Column(name = "creation_user")
    private UUID creationUser;

    /**
     * Дата и время изменения записи
     */
    @Column(name = "change_time")
    private LocalDateTime changeTime;

    /**
     * Пользователь, изменивший запись
     */
    @Column(name = "change_user")
    private UUID changeUser;

    /**
     * Серия документа
     */
    @Column
    private String seria;

    /**
     * Номер документа
     */
    @Column
    private String number;

    /**
     * Дата выдачи документа
     */
    @Column(name = "issue_date_document")
    private LocalDateTime issueDateDocument;

    /**
     * Кем выдан
     */
    @Column
    private String issue;

    /**
     * Где выдан
     */
    @Column(name = "place_issue")
    private String placeIssue;

    /**
     * Время начала действия документа
     */
    @Column(name = "start_time_document")
    private LocalDateTime startTimeDocument;

    /**
     * Время окончания действия документа
     */
    @Column(name = "final_time_document")
    private LocalDateTime finalTimeDocument;

    /**
     * Категории в водительском удостоверении
     */
    @Column(name = "categoria")
    private String categoria;

    /**
     * Цвет транспортного средства
     */
    @Column(name = "color")
    private String color;

    /**
     * Количество пассажирских мест
     */
    @Column(name = "passenger_seats_count")
    private Integer passengerSeatsCount;

    /**
     * Государственный регистрационный знак транспортного средства
     */
    @Column(name = "registration_number")
    private String registrationNumber;

    /**
     * VIN транспортного средства
     */
    @Column(name = "vin")
    private String vin;

    /**
     * Объем двигателя
     */
    @Column(name = "engine_volume")
    private Integer engineVolume;

    /**
     * Мощность двигателя
     */
    @Column(name = "engine_power")
    private String enginePower;

    /**
     * Марка траспортного средства
     */
    @Column(name = "brand_name")
    private String brandName;

    /**
     * Моедль транспортного средства
     */
    @Column(name = "model")
    private String model;

    public void updateAuthorInfo(UUID currentUser) {
        if (getCreationTime() == null) {
            setCreationTime(LocalDateTime.now());
            setCreationUser(currentUser);
        } else {
            setChangeTime(LocalDateTime.now());
            setChangeUser(currentUser);
        }
    }
}
