package ru.sberbank.ditsib.transport.sudir.web.mappers;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.sudir.web.model.Token;

/**
 * Маппинг токенов из бизнес в веб.
 */
@Mapper
public interface TokenMapper {
    
    Token toDto(ru.sberbank.ditsib.transport.sudir.business.model.Token source);
    
}
