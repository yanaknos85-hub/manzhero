package ru.sberbank.ditsib.transport.approvals.controller.settings;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sberbank.ditsib.transport.approvals.dto.settings.GetLimitApprovalSettingsDTO;

import java.util.UUID;

/**
 * Контроллер для работы с настройками согласований заявок на лимит для корп.клиента
 **/
@Deprecated
@RequestMapping({"{organizationId}/settings/limit", "{organizationId}/settings/limit/"})
@Tag(name = "Настройки согласований заявок на лимит", description = "Модуль управления согласованиями")
public interface LimitApprovalsSettingsController {
    
    // todo пока что все запросы - заглушки. Служат только лишь для отбражения информации фронтами
    
    /**
     * Получить настройки согласований заявок на лимит
     * @return настройки согласований заявок на лимит
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получить настройки согласований заявок на лимит",
               description = "Получить настройки согласований заявок на лимит")
    GetLimitApprovalSettingsDTO get(@PathVariable UUID organizationId);
}