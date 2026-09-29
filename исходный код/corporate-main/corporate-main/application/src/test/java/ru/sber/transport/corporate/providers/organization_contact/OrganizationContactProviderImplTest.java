package ru.sber.transport.corporate.providers.organization_contact;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.Contact;
import ru.sber.transport.corporate.business.model.Organization;
import ru.sber.transport.corporate.business.providers.OrganizationContactProvider;
import ru.sber.transport.database.corporate.enums.ContactType;
import ru.sber.transport.database.corporate.tables.OrganizationContacts;
import ru.sber.transport.database.corporate.tables.records.ContactRecord;
import ru.sber.transport.database.corporate.tables.records.OrganizationRecord;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка провайдера связки контактов")
@JooqTest
@ContextConfiguration(classes = {JooqDatabaseConfig.class, OrganizationContactProviderImpl.class})
@ActiveProfiles("test")
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
class OrganizationContactProviderImplTest {

    @Autowired
    private OrganizationContactProvider provider;

    @Autowired
    private DSLContext context;

    @Test
    @DisplayName("Проверка связи организации и контакта")
    void test_link() {
        var dbOrganization = context.insertInto(ru.sber.transport.database.corporate.tables.Organization.ORGANIZATION)
                .set(new OrganizationRecord(UUID.randomUUID(), Instancio.create(String.class), Instancio.create(String.class), null, null,Instancio.create(String.class),Instancio.create(String.class),null,null,null))
                .returning()
                .fetchSingle();
        var dbContact = context.insertInto(ru.sber.transport.database.corporate.tables.Contact.CONTACT)
                .set(new ContactRecord(UUID.randomUUID(), Instancio.create(ContactType.class), Instancio.create(String.class)))
                .returning()
                .fetchSingle();

        var organization = Instancio.of(Organization.class).set(Select.field(Organization::getId), dbOrganization.getId()).create();
        var contact = Instancio.of(Contact.class).set(Select.field(Contact::getId), dbContact.getId()).create();

        provider.link(organization, contact);

        assertThat(context.fetchCount(context.select().from(OrganizationContacts.ORGANIZATION_CONTACTS))).isEqualTo(1);

        var actual = context.selectFrom(OrganizationContacts.ORGANIZATION_CONTACTS).fetchSingle();

        assertThat(actual.getOrganizationId()).isEqualTo(organization.getId());
        assertThat(actual.getContactId()).isEqualTo(contact.getId());
    }
}