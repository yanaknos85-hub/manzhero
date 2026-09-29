package ru.sberbank.ditsib.transport.approvals.dto.settings;

import java.util.UUID;

/**
 * Генератор стабов для контроллеров настроек согласований
 */
@Deprecated
public class ApprovalSettingsStabSharedData {
    
    /**
     * Создать GetLimitApprovalSettingsDTO
     * @param organizationId ID корп.клиента
     * @return GetLimitApprovalSettingsDTO
     */
    public GetLimitApprovalSettingsDTO createGetLimitApprovalSettingsDTO(UUID organizationId) {
        return GetLimitApprovalSettingsDTO.builder()
                                          .organizationId(organizationId)
                                          .build();
    }
    
}
