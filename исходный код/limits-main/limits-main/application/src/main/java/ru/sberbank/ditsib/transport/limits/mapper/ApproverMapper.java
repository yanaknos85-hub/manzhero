package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.limits.dto.ApproverDTO;
import ru.sberbank.ditsib.transport.limits.model.limit.Approver;

/**
 * Mapper for approver data.
 */
@Mapper
public interface ApproverMapper {

    /**
     * Convert model to business.
     *
     * @param source database model.
     * @return business entity.
     */
    ApproverDTO toDto(Approver source);
    
}
