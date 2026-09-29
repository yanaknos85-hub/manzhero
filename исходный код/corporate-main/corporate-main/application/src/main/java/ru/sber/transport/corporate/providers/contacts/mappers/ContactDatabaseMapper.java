package ru.sber.transport.corporate.providers.contacts.mappers;

import org.mapstruct.Mapper;
import ru.sber.transport.corporate.business.model.Contact;
import ru.sber.transport.database.corporate.tables.records.ContactRecord;

/// Маппер объектов бизнес модели и базы данных
@Mapper
public interface ContactDatabaseMapper {

     /// Преобразовать бизнес-объект в базу данных
     ///
     /// @param source бизнес-объект
     /// @return объект базы
    ContactRecord toDatabase(Contact source);

    /// Преобразовать базу данных в бизнес-объект
    ///
    /// @param source объект базы
     ///
     /// @return бизнес-объект
    Contact toBusiness(ContactRecord source);

}
