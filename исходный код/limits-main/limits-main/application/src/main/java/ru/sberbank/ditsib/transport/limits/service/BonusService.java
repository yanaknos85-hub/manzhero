package ru.sberbank.ditsib.transport.limits.service;

import org.springframework.lang.NonNull;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.model.bonus.Bonus;
import ru.sberbank.ditsib.transport.limits.model.bonus.BonusRequest;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с бонусным счётом
 */
public interface BonusService {
    
    /**
     * Получить данные о бонусном счёте
     *
     * @param ownerId id сотрудника
     *
     * @return данные о бонусном счёте
     */
    Bonus get(@NonNull UUID ownerId);
    
    /**
     * Пополнение бонусного счёта
     *
     * @param ownerId id сотрудника
     * @param sum сумма пополнения
     * @param humanReadableId клиентский id
     * @param transportType тип транспорта
     *
     * @return запрос на пополнение
     */
    BonusRequest deposit(
            @NonNull UUID ownerId,
            @NonNull BigDecimal sum,
            @NonNull String humanReadableId,
            @NonNull TransportTypeEnum transportType
                        );
    
    /**
     * Резервирование бонусного счёта
     *
     * @param ownerId id сотрудника
     * @param sum сумма резервирования
     * @param requestId id запроса (сервиса request)
     * @param humanReadableId клиентский id
     * @param transportType тип транспорта
     * @param limitCheck только проверка средств на счету
     *
     * @return запрос на резервирование
     */
    BonusRequest reserve(
            @NonNull UUID ownerId,
            @NonNull BigDecimal sum,
            @NonNull UUID requestId,
            @NonNull String humanReadableId,
            @NonNull TransportTypeEnum transportType,
            boolean limitCheck
                        );
    
    /**
     * Снять зарезервированную сумму со счёта
     *
     * @param request запрос на резервирование
     */
    void spend(@NonNull BonusRequest request);
    
    /**
     * Отменить резервирование
     *
     * @param request
     */
    void cancel(@NonNull BonusRequest request);
    
    /**
     * Получить бонусную заявку
     *
     * @param requestId идентификатор связанного Request
     *
     * @return bonusRequest
     */
    Optional<BonusRequest> getByRequestId(UUID requestId);
}
