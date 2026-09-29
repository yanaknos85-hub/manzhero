package ru.sber.transport.corporate.business.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.Organizations;
import ru.sber.transport.corporate.business.model.Contact;
import ru.sber.transport.corporate.business.model.Organization;
import ru.sber.transport.corporate.business.providers.ContactProvider;
import ru.sber.transport.corporate.business.providers.OrganizationContactProvider;
import ru.sber.transport.corporate.messaging.senders.OrganizationSender;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.utils.DataCreator;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка бизнес-кейсов организаций")
class OrganizationsImplTest implements DataCreator {

    private final ru.sber.transport.corporate.business.providers.OrganizationProvider organizationProvider = mock(ru.sber.transport.corporate.business.providers.OrganizationProvider.class);

    private final ContactProvider contactProvider = mock(ContactProvider.class);

    private final OrganizationContactProvider organizationContactProvider = mock(OrganizationContactProvider.class);

    private final OrganizationSender organizationSender = mock(OrganizationSender.class);

    private final Organizations organizations = new OrganizationsImpl(organizationProvider, contactProvider, organizationContactProvider, organizationSender);

    @Test
    @DisplayName("Добавление организации - дубликат ЕАСУП")
    void test_add_duplicate_easup() {
        var source = Instancio.create(Organization.class);

        when(organizationProvider.existsSync(source.getSyncId())).thenReturn(true);

        assertThatThrownBy(() -> organizations.add(source))
                .isInstanceOf(DuplicateDataException.class)
                .hasFieldOrPropertyWithValue("entityName", "Organization")
                .hasFieldOrPropertyWithValue("values", Map.of("easupId", source.getSyncId()));
    }

    @Test
    @DisplayName("Добавление организации - дубликат названия")
    void test_add_duplicate_name() {
        var source = Instancio.create(Organization.class);

        when(organizationProvider.exists(source.getName())).thenReturn(true);

        assertThatThrownBy(() -> organizations.add(source))
                .isInstanceOf(DuplicateDataException.class)
                .hasFieldOrPropertyWithValue("entityName", "Organization")
                .hasFieldOrPropertyWithValue("values", Map.of("officialName", source.getName()));
    }

    @Test
    @DisplayName("Добавление организации - успех")
    void test_add() {
        var source = Instancio.create(Organization.class);
        var saved = createOrganization(source);

        when(organizationProvider.save(source)).thenReturn(saved);
        when(contactProvider.save(any())).then(i -> createContact(i.getArgument(0, Contact.class)));

        var actual = organizations.add(source);

        var contactsCaptor = ArgumentCaptor.forClass(Contact.class);
        var linkOrganizationCaptor = ArgumentCaptor.forClass(Organization.class);
        var linkContactCaptor = ArgumentCaptor.forClass(Contact.class);
        var organizationCaptor = ArgumentCaptor.forClass(Organization.class);

        verify(organizationProvider).save(source);
        verify(contactProvider, times(source.getContacts().size())).save(contactsCaptor.capture());
        verify(organizationContactProvider, times(source.getContacts().size())).link(linkOrganizationCaptor.capture(), linkContactCaptor.capture());
        verify(organizationSender).send(organizationCaptor.capture());

        var contacts = contactsCaptor.getAllValues();
        var linkOrganizations = linkOrganizationCaptor.getAllValues();
        var linkContacts = linkContactCaptor.getAllValues();
        var organizations = organizationCaptor.getAllValues();

        assertThat(organizations.getLast()).isNotNull();
        assertThat(actual).isEqualTo(saved);

        for (var i = 0; i < source.getContacts().size(); i++) {
            var actualContact = contacts.get(i);
            var expectedContact = source.getContacts().get(i);
            assertSoftly(it -> {
                it.assertThat(actualContact.getId()).isEqualTo(expectedContact.getId());
                it.assertThat(actualContact.getValue()).isEqualTo(expectedContact.getValue());
                it.assertThat(actualContact.getType()).isEqualTo(expectedContact.getType());
            });
        }

        assertThat(linkOrganizations).hasSize(source.getContacts().size());
        assertThat(linkContacts).hasSize(source.getContacts().size());

        for (var i = 0; i < linkOrganizations.size(); i++) {
            var actualOrganization = linkOrganizations.get(i);
            var actualContact = linkContacts.get(i);
            var expectedContact = source.getContacts().get(i);
            assertSoftly(it -> {
                it.assertThat(actualOrganization.getId()).isEqualTo(saved.getId());
                it.assertThat(actualOrganization.getSyncId()).isEqualTo(saved.getSyncId());
                it.assertThat(actualContact.getId()).isEqualTo(expectedContact.getId());
            });
        }
    }

}