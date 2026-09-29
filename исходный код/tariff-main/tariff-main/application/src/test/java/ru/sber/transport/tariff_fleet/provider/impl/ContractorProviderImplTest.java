package ru.sber.transport.tariff_fleet.provider.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.contractor.messages.ContractorMessage;
import ru.sber.transport.tariff_fleet.constant.ContractorType;
import ru.sber.transport.tariff_fleet.constant.ServiceType;
import ru.sber.transport.tariff_fleet.database.model.Contractor;
import ru.sber.transport.tariff_fleet.service.ContractorService;

import java.util.Optional;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка провайдера контрагентов")
class ContractorProviderImplTest {
    
    @InjectMocks
    private ContractorProviderImpl contractorProvider;
    @Mock
    private ContractorService contractorService;
    
    @Test
    void delete() {
        var contractorMessage = Instancio.of(ContractorMessage.class)
                                         .set(field(ContractorMessage::deleted), true)
                                         .create();
        var contractor = Instancio.of(Contractor.class).create();
        doReturn(Optional.of(contractor)).when(contractorService).get(contractorMessage.getId());
        contractorProvider.delete(contractorMessage);
        verify(contractorService).delete(contractor);
    }
    
    @Test
    void save() {
        var contractorMessage = Instancio.of(ContractorMessage.class)
                                         .set(field(ContractorMessage::deleted), false)
                                         .set(field(ContractorMessage::contractorType), ContractorType.API.name())
                                         .set(field(ContractorMessage::serviceType), ServiceType.AUTOSERVICE.name())
                                         .create();
        contractorProvider.save(contractorMessage);
        verify(contractorService).save(any(Contractor.class));
        
        var contractorMessageNullId = Instancio.of(ContractorMessage.class)
                                               .set(field(ContractorMessage::getId), null)
                                               .set(field(ContractorMessage::deleted), false)
                                               .set(field(ContractorMessage::contractorType), null)
                                               .set(field(ContractorMessage::serviceType), null)
                                               .create();
        contractorProvider.save(contractorMessageNullId);
        verifyNoMoreInteractions(contractorService);
        
        var contractorMessageNullName = Instancio.of(ContractorMessage.class)
                                                 .set(field(ContractorMessage::name), null)
                                                 .set(field(ContractorMessage::deleted), false)
                                                 .set(field(ContractorMessage::contractorType), null)
                                                 .set(field(ContractorMessage::serviceType), null)
                                                 .create();
        contractorProvider.save(contractorMessageNullName);
        verifyNoMoreInteractions(contractorService);
    }
}
