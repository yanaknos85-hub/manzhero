package ru.sber.transport.authorization.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Модель данных для проверки подписания ПДн.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConsentCheckModel {

    /**
     * ID пользователя.
     */
    private UUID id;

    /**
     * Список ролей пользователя.
     */
    private List<String> roles;

}
