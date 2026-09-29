package ru.sber.transport.corporate.business.providers;

import ru.sber.transport.corporate.business.model.Contact;
import ru.sber.transport.corporate.business.model.Organization;

/**
 * Интерфейс для связи контактов и организаций
 */
public interface OrganizationContactProvider {

    /**
     * Связывает контакт с организацией
     *
     * @param contact      контакт
     * @param organization организация
     */
    void link(Organization organization, Contact contact);
}
