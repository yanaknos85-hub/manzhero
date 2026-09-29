package ru.sber.transport.limits.web.grpc.client.mappers;

import org.mapstruct.Mapper;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;

import java.util.List;

/**
 * Mapper для маппинга контактов в gRPC.
 */
@Mapper
public interface ContactGrpcMapper {

    /**
     * Маппинг объекта в бизнес.
     *
     * @param source исходный объект
     * @return бизнес-объект
     */
    default String toBusiness(List<OrganizationsOuterClass.Contact> source) {
        return source.stream()
                .filter(it -> OrganizationsOuterClass.ContactType.EMAIL.equals(it.getType()))
                .map(OrganizationsOuterClass.Contact::getValue)
                .findFirst()
                .orElse(null);
    }

}
