package ru.sber.transport.corporate.web.grpc.mappers;

import org.mapstruct.*;
import ru.sber.transport.corporate.business.model.Contact;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Маппер данных бизнес - gRPC.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContactsGrpcMapper {

    /**
     * Преобразование бизнес в gRPC.
     *
     * @param source бизнес.
     * @return gRPC.
     */
    @Mapping(target = "valueBytes", ignore = true)
    @Mapping(target = "internal", constant = "false")
    OrganizationsOuterClass.Contact toGrpc(Contact source);

    /**
     * Преобразование бизнес в gRPC.
     *
     * @param source бизнес.
     * @return gRPC.
     */
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<OrganizationsOuterClass.Contact> toGrpc(List<Contact> source);

    default List<OrganizationsOuterClass.Contact> toGrpc(Employee source) {
        if (source == null) {
            return Collections.emptyList();
        }
        final var contacts = new ArrayList<OrganizationsOuterClass.Contact>();
        final var mobilePhone = source.getPhone();
        if (mobilePhone != null) {
            var phone = OrganizationsOuterClass.Contact.newBuilder()
                    .setType(OrganizationsOuterClass.ContactType.PHONE)
                    .setValue(mobilePhone)
                    .setIsConfirmed(source.isPhoneConfirmed())
                    .setInternal(false)
                    .build();

            contacts.add(phone);
        }
        final var internalEmail = source.getEmail();
        if (internalEmail != null) {
            var email = OrganizationsOuterClass.Contact.newBuilder()
                    .setType(OrganizationsOuterClass.ContactType.EMAIL)
                    .setValue(internalEmail)
                    .setIsConfirmed(false)
                    .setInternal(true)
                    .build();

            contacts.add(email);
        }
        final var externalEmail = source.getExternalEmail();
        if (externalEmail != null) {
            var email = OrganizationsOuterClass.Contact.newBuilder()
                    .setType(OrganizationsOuterClass.ContactType.EMAIL)
                    .setValue(internalEmail)
                    .setIsConfirmed(false)
                    .setInternal(false)
                    .build();

            contacts.add(email);
        }
        return contacts;
    }

}
