package ru.sber.transport.limits.web.providers;

import ru.sber.transport.limits.business.model.LimitSharing;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Провайдер распределений
 */
public interface SharingsProvider {

    /**
     * Получить распределений по идентификаторам лимитов
     *
     * @param ids идентификаторы лимитов
     * @return распределения в связке с лимитами
     */
    Map<UUID, List<LimitSharing>> get(List<UUID> ids);

}
