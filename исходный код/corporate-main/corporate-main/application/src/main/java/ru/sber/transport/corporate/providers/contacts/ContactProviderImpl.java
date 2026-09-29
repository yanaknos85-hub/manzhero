package ru.sber.transport.corporate.providers.contacts;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.corporate.business.model.Contact;
import ru.sber.transport.corporate.business.providers.ContactProvider;
import ru.sber.transport.database.corporate.tables.records.ContactRecord;
import ru.sber.transport.corporate.providers.contacts.mappers.ContactDatabaseMapper;

import java.util.UUID;

@Repository
@Transactional
@RequiredArgsConstructor
class ContactProviderImpl implements ContactProvider, JooqRepository<ru.sber.transport.database.corporate.tables.Contact, ContactRecord, UUID> {

    private final ContactDatabaseMapper mapper;

    @Override
    public Contact save(Contact contact) {
        var toSave = mapper.toDatabase(contact);
        if (toSave.getId() == null) {
            toSave.setId(UUID.randomUUID());
        }
        return mapper.toBusiness(save(toSave));
    }

    @Override
    public ru.sber.transport.database.corporate.tables.Contact table() {
        return ru.sber.transport.database.corporate.tables.Contact.CONTACT;
    }
}
