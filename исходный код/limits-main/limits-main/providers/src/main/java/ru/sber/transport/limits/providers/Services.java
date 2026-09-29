package ru.sber.transport.limits.providers;

import ru.sber.transport.limits.model.Service;

import java.util.List;
import java.util.Optional;

/**
 * Провайдер услуг
 */
public interface Services {

    /**
     * Возвращает услугу по ее названию
     *
     * @param service название услуги
     * @return услуга
     */
    Optional<Service> get(String service);

    /**
     * Возвращает все услуги
     *
     * @return услуги
     */
    List<Service> get();
}
