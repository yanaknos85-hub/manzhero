package ru.sber.transport.limits.web.http.mappers;

import org.mapstruct.Mapper;
import ru.sber.transport.limits.business.model.Status;

/**
 * Маппинг статусов
 */
@Mapper
public interface StatusWebMapper {

    /**
     * Преобразовать статус из веб-слоя в бизнес-модель
     *
     * @param status веб-слой
     * @return бизнес-модель
     */
    default Status toBusiness(ru.sber.transport.limits.web.model.Status status) {
        if (status == null) {
            return null;
        }
        return Status.valueOf(status.name());
    }

    /**
     * Преобразовать статус в модель для веб-слоя
     *
     * @param status статус
     * @return модель для веб-слоя
     */
    default ru.sber.transport.limits.web.model.Status toWeb(Status status) {
        if (status == null) {
            return null;
        }
        return ru.sber.transport.limits.web.model.Status.valueOf(status.name());
    }

}
