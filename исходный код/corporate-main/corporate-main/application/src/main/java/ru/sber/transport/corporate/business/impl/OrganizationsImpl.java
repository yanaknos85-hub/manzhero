package ru.sber.transport.corporate.business.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.corporate.business.Organizations;
import ru.sber.transport.corporate.business.model.Active;
import ru.sber.transport.corporate.business.model.Contact;
import ru.sber.transport.corporate.business.model.Organization;
import ru.sber.transport.corporate.business.providers.ContactProvider;
import ru.sber.transport.corporate.business.providers.OrganizationContactProvider;
import ru.sber.transport.corporate.business.providers.OrganizationProvider;
import ru.sber.transport.corporate.messaging.senders.OrganizationSender;
import ru.sber.transport.exceptions.DuplicateDataException;

import java.util.ArrayList;

@Transactional
@RequiredArgsConstructor
@Component
class OrganizationsImpl implements Organizations {

    private final OrganizationProvider organizationProvider;

    private final ContactProvider contactProvider;

    private final OrganizationContactProvider organizationContactProvider;

    private final OrganizationSender sender;

    @Override
    public Organization add(Organization source) {
        var syncId = source.getSyncId();
        if (syncId != null && organizationProvider.existsSync(syncId)) {
            throw new DuplicateDataException(Organization.class, "easupId", syncId);
        }
        var name = source.getName();
        if (organizationProvider.exists(name)) {
            throw new DuplicateDataException(Organization.class, "officialName", name);
        }
        if (source.getStatus() == null) {
            source.setStatus(Active.ACTIVE);
        }
        var saved = organizationProvider.save(source);
        var savedContacts = new ArrayList<Contact>();
        for (var contact : source.getContacts()) {
            contact = contactProvider.save(contact);
            savedContacts.add(contact);
            organizationContactProvider.link(source, contact);
        }
        saved.setContacts(savedContacts);
        sender.send(saved);
        return saved;
    }

}
