package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.limits.dto.BonusDTO;
import ru.sberbank.ditsib.transport.limits.dto.BonusRequestDTO;
import ru.sberbank.ditsib.transport.limits.model.bonus.Bonus;
import ru.sberbank.ditsib.transport.limits.model.bonus.BonusRequest;

/**
 * Маппер для работы с бонусами
 */
@Mapper
public interface BonusMapper {
    
    BonusDTO toBonusDTO(Bonus bonus);
    
    BonusRequestDTO toBonusRequestDTO(BonusRequest bonusRequest);
    
}
