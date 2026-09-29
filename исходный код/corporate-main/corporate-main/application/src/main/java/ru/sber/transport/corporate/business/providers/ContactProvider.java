package ru.sber.transport.corporate.business.providers;

import ru.sber.transport.corporate.business.model.Contact;

/// Провайдер контактов
public interface ContactProvider {

    /// Сохранение контакта
    ///
    /// @param contact контакт
     ///
    /// @return сохраненный контакт
    Contact save(Contact contact);

}
