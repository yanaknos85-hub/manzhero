package ru.sberbank.ditsib.transport.approvals.messaging.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

/**
 * Message with create/delete request document
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestDocumentMessage implements Message<UUID> {
    
    /**
     * request id
     */
    private UUID requestId;
    
    /**
     * document id
     */
    private UUID documentId;
    
    /**
     * employee id than document downloaded
     */
    private UUID employeeId;
    
    private boolean deleted;
    
    @JsonIgnore
    @Override
    public UUID getId() {
        return requestId;
    }
}
