package ru.sber.transport.corporate.messaging.senders.mappers;

import org.mapstruct.Mapper;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.messages.corporate.avro.Contact;
import ru.sber.transport.messages.corporate.avro.ContactEmployeeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Маппер сообщений контактов
 */
@Mapper
public interface ContactMessageMapper {

    /**
     * Конвертация модели в сообщение.
     *
     * @param source исходные данные
     * @return сообщение.
     */
    default List<Contact> toMessage(Employee source) {
        if (source == null) {
            return Collections.emptyList();
        }

        return mapAvroContacts(source.getEmail(), source.getExternalEmail(), source.getPhone(),
                source.isPhoneConfirmed());
    }

    /**
     * Конвертация модели в сообщение.
     *
     * @param source исходные данные
     * @return сообщение.
     */
    default List<Contact> toMessage(ru.sberbank.ditsib.corpclient.database.model.Employee source) {
        if (source == null) {
            return Collections.emptyList();
        }

        return mapAvroContacts(source.getEmail(), source.getExternalEmail(), source.getMobilePhone(),
                source.isPhoneConfirmed());
    }

    /**
     * Маппинг контактных данных на авро контакт.
     *
     * @param internalEmail внутренний email.
     * @param externalEmail внешний email.
     * @param mobilePhone мобильный телефон.
     * @param isPhoneConfirmed подтвержден ли номер телефона.
     * @return сообщение.
     */
    default List<Contact> mapAvroContacts(String internalEmail, String externalEmail, String mobilePhone,
                                          boolean isPhoneConfirmed) {

        final var contacts = new ArrayList<Contact>();
        if (mobilePhone != null) {
            var phone = new Contact();
            phone.setType(ContactEmployeeType.MOBILE);
            phone.setValue(mobilePhone);
            phone.setIsConfirmed(isPhoneConfirmed);
            phone.setInternal(false);
            contacts.add(phone);
        }
        if (internalEmail != null) {
            var email = new Contact();
            email.setType(ContactEmployeeType.EMAIL);
            email.setValue(internalEmail);
            email.setInternal(true);
            email.setIsConfirmed(false);
            contacts.add(email);
        }
        if (externalEmail != null) {
            var email = new Contact();
            email.setType(ContactEmployeeType.EMAIL);
            email.setValue(externalEmail);
            email.setInternal(false);
            email.setIsConfirmed(false);
            contacts.add(email);
        }
        return contacts;
    }


}
