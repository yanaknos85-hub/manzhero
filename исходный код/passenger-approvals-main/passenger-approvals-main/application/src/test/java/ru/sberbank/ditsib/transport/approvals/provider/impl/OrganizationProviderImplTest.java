package ru.sberbank.ditsib.transport.approvals.provider.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.approvals.database.model.Organization;
import ru.sberbank.ditsib.transport.approvals.mappers.OrganizationMapper;
import ru.sberbank.ditsib.transport.approvals.services.OrganizationService;
import ru.sberbank.ditsib.transport.messaging.messages.ContactMessage;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка провайдера организаций")
class OrganizationProviderImplTest {
    
    @InjectMocks
    private OrganizationProviderImpl provider;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private OrganizationMapper mapper;
    
    @Test
    void delete() {
        var message = Instancio.create(OrganizationMessage.class);
        var entity = Instancio.create(Organization.class);
        doReturn(entity).when(mapper).organizationMessageToOrganization(message);
        doNothing().when(organizationService).delete(entity);
        provider.delete(message);
        verify(mapper).organizationMessageToOrganization(any(OrganizationMessage.class));
        verify(organizationService).delete(any(Organization.class));
    }
    
    @Test
    void save() {
        var contact1 = Instancio.create(ContactMessage.class);
        var contact2 = Instancio.create(ContactMessage.class);
        var message = Instancio.create(OrganizationMessage.class);
        message.setContacts(List.of(contact1, contact2));
        var entity = Instancio.create(Organization.class);
        doReturn(entity).when(mapper).organizationMessageToOrganization(message);
        doNothing().when(organizationService).save(entity);
        provider.save(message);
        verify(mapper).organizationMessageToOrganization(any(OrganizationMessage.class));
        verify(organizationService).save(any(Organization.class));
    }
}