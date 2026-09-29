package ru.sber.transport.limits.providers;

import ru.sber.transport.limits.model.Type;

import java.util.Optional;

/**
 * Провайдер типов услуг
 */
public interface Types {

    /**
     * Возвращает тип услуги по его названию
     *
     * @param type название типа услуги
     * @return тип услуги
     */
    Optional<Type> get(String type);
}
