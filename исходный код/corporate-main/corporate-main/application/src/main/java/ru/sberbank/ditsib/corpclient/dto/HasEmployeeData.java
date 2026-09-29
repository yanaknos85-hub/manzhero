package ru.sberbank.ditsib.corpclient.dto;

import java.util.Set;
import java.util.UUID;

/**
 * Интерфейс для объектов сотрудников.
 */
public interface HasEmployeeData {

    /**
     * @return имя.
     */
    String firstName();

    /**
     * @return фамилия.
     */
    String lastName();

    /**
     * @return отчество.
     */
    String patronymic();

    /**
     * @return табельный номер.
     */
    String personnelNumber();

    /**
     * @return идентификатор должности.
     */
    UUID positionId();

    /**
     * @return номер телефона.
     */
    String mobilePhone();

    /**
     * @return e-mail.
     */
    String email();

    /**
     * @return идентификатор рук-ля.
     */
    UUID supervisorId();

    /**
     * @return список ролей.
     */
    Set<String> roles();

    /**
     * @return идентификатор сотрудника.
     */
    UUID id();

    /**
     * @return идентификатор прикрепленного пользователя.
     */
    UUID userId();

    /**
     * @return человекочитаемый идентификатор.
     */
    String humanReadableId();
}
