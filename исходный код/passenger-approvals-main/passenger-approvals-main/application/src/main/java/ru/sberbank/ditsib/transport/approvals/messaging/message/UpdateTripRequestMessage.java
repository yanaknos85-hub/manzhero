package ru.sberbank.ditsib.transport.approvals.messaging.message;

import lombok.*;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

/**
 * Update trip request message. Используется при формировании запроса на изменение уже согласованной заявки (при этом
 * сама заявка не меняется)
 **/
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTripRequestMessage implements Message<UUID> {
    private UUID id;
    
    private RequestMessage request;
    
    private boolean deleted;
}
