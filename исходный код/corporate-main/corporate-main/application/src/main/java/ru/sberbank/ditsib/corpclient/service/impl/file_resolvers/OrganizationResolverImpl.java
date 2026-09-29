package ru.sberbank.ditsib.corpclient.service.impl.file_resolvers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.file_works.importer.DataImporter;
import ru.sberbank.ditsib.corpclient.database.model.Contact;
import ru.sberbank.ditsib.corpclient.database.model.ContactType;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.dto.NewOrganizationFileDTO;
import ru.sberbank.ditsib.corpclient.messaging.sender.OrganizationSender;
import ru.sberbank.ditsib.corpclient.service.OrganizationService;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Сервис для распознавания и записи информации о сотруднике.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
class OrganizationResolverImpl implements DataImporter<NewOrganizationFileDTO>, DataExporter<NewOrganizationFileDTO> {

    private final OrganizationService organizationService;

    private final OrganizationSender sender;

    @Override
    public void importData(NewOrganizationFileDTO item, @NonNull Map<String, ?> parameters, @NonNull JwtAuthenticationToken authentication) {
        var organization = organizationService.get(item.getOfficialName())
                .orElseGet(Organization::new);

        organization.setAddress(item.getAddress());
        organization.setOfficialName(item.getOfficialName());
        organization.setMsrn(item.getMsrn());
        organization.setTid(item.getTin());
        organization.setOrganizationCode(item.getOrganizationCode());
        organization.getContacts().addAll(createEmail(item.getEmail()));
        organization.getContacts().addAll(createPhones(item.getPhones()));
        organization.getContacts().addAll(createSites(item.getSites()));

        if (organization.getId() == null) {
            organizationService.add(organization);
        } else {
            organizationService.edit(organization.getId(), organization);
        }
    }

    @Override
    public @NonNull List<NewOrganizationFileDTO> exportData(@NonNull Map<String, ?> parameters, @NonNull JwtAuthenticationToken authentication) {
        var result = new ArrayList<NewOrganizationFileDTO>();
        for (var item : organizationService.getAll()) {
            var resItem = new NewOrganizationFileDTO();

            resItem.setAddress(item.getAddress());
            resItem.setOfficialName(item.getOfficialName());
            resItem.setMsrn(item.getMsrn());
            resItem.setTin(item.getTid());
            resItem.setOrganizationCode(item.getOrganizationCode());
            for (var contact : item.getContacts()) {
                Function<NewOrganizationFileDTO, String> getter;
                BiConsumer<NewOrganizationFileDTO, String> setter;
                switch (contact.getContactType()) {
                    case EMAIL -> {
                        getter = NewOrganizationFileDTO::getEmail;
                        setter = NewOrganizationFileDTO::setEmail;
                    }
                    case PHONE -> {
                        getter = NewOrganizationFileDTO::getPhones;
                        setter = NewOrganizationFileDTO::setPhones;
                    }
                    case SITE -> {
                        getter = NewOrganizationFileDTO::getSites;
                        setter = NewOrganizationFileDTO::setSites;
                    }
                    default -> {
                        log.info("%s is not a known type".formatted(contact.getContactType()));
                        continue;
                    }
                }
                String contactStr = getter.apply(resItem);
                if (contactStr != null) {
                    var contactBuilder = new StringBuilder(contactStr);
                    if (!contactBuilder.toString().isEmpty()) {
                        contactBuilder.append(", ");
                    }
                    contactBuilder.append(contact.getValue());
                    setter.accept(resItem, contactBuilder.toString());
                }
            }
            result.add(resItem);
        }
        return result;
    }

    private Collection<? extends Contact> createEmail(String emails) {
        return createContacts(ContactType.EMAIL, emails);
    }

    private Collection<? extends Contact> createPhones(String phones) {
        return createContacts(ContactType.PHONE, phones);
    }

    private Collection<? extends Contact> createSites(String sites) {
        return createContacts(ContactType.SITE, sites);
    }

    private Collection<? extends Contact> createContacts(ContactType type, String value) {
        if (value != null) {
            return Arrays.stream(value.split(", "))
                    .map(contact -> createContact(type, value))
                    .toList();
        }
        return Collections.emptyList();
    }

    private Contact createContact(ContactType type, String value) {
        var contact = new Contact();

        contact.setContactType(type);
        contact.setValue(value);

        return contact;
    }
}
