package ru.sberbank.ditsib.corpclient.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import ru.sberbank.ditsib.corpclient.dto.ContactDto;
import ru.sberbank.ditsib.corpclient.dto.ContactType;
import ru.sberbank.ditsib.corpclient.database.model.Contact;
import ru.sberbank.ditsib.transport.messaging.messages.ContactMessage;

import java.util.List;
import java.util.Optional;

/**
 * Маппер контактов.
 */
@Mapper
public interface ContactMapper {
    
    default String toMessage(ru.sberbank.ditsib.corpclient.database.model.ContactType source) {
        return Optional.ofNullable(source).map(Enum::name).orElse(null);
    }
    
    @Mapping(target = "type", source = "contactType")
    ContactDto toDto(Contact source);

    /**
     * Преобразовать контакт в сообщение
     *
     * @param source контакт
     * @return сообщение
     */
    @Mapping(target = "type", source = "contactType")
    ContactMessage toMessage(Contact source);

    /**
     * Преобразовать контакт в сообщение
     *
     * @param source контакт
     * @return сообщение
     */
    @Mapping(target = "type", source = "contactType")
    ru.sber.transport.messages.corporate.avro.ContactMessage toMessageAvro(Contact source);
    
    ContactType toDto(ru.sberbank.ditsib.corpclient.database.model.ContactType source);
    
    @Mapping(target = "contactType", source = "type")
    Contact toModel(ContactDto source);
    
    ru.sberbank.ditsib.corpclient.database.model.ContactType toModel(ContactType source);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<ContactMessage> toMessage(List<Contact> source);
    
}
