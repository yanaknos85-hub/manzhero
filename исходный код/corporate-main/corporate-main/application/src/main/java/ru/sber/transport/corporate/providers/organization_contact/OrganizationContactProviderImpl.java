package ru.sber.transport.corporate.providers.organization_contact;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.corporate.business.model.Contact;
import ru.sber.transport.corporate.business.model.Organization;
import ru.sber.transport.corporate.business.providers.OrganizationContactProvider;
import ru.sber.transport.database.corporate.tables.OrganizationContacts;
import ru.sber.transport.database.corporate.tables.records.OrganizationContactsRecord;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
class OrganizationContactProviderImpl implements OrganizationContactProvider, JooqRepository<OrganizationContacts, OrganizationContactsRecord, UUID> {

    @Override
    public void link(Organization organization, Contact contact) {
        var link = new OrganizationContactsRecord(contact.getId(), organization.getId());
        context().insertInto(table())
                .set(link)
                .onConflict(table().CONTACT_ID, table().ORGANIZATION_ID)
                .doUpdate()
                .set(link).execute();
    }

    @Override
    public OrganizationContacts table() {
        return OrganizationContacts.ORGANIZATION_CONTACTS;
    }
}
