package ru.sberbank.ditsib.transport.sudir.providers.refresh.mapper;

import org.mapstruct.Mapper;
import ru.sber.transport.database.sudir.tables.records.RoleRecord;
import ru.sberbank.ditsib.transport.sudir.business.model.Account;
import ru.sber.transport.database.sudir.tables.records.AccountRecord;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Маппер УЗ из бд в бизнес.
 */
@Mapper
public interface SessionMapper {

    /**
     * Convert database to business entity.
     *
     * @param source database entity.
     * @param roles roles.
     * @return business entity.
     */
    default Account toBusiness(AccountRecord source, Set<RoleRecord> roles) {
        return Account.builder()
                         .id(source.getId())
                         .personnelNumber(source.getPersonnelNumber())
                         .roles(roles.stream().collect(Collectors.toMap(RoleRecord::getCode, RoleRecord::getDataMaster)))
                         .build();
    }

    /**
     * Convert database to business entity.
     *
     * @param role database entity.
     * @return business entity.
     */
    default String toBusiness(RoleRecord role) {
        return Optional.ofNullable(role).map(RoleRecord::getCode).orElse(null);
    }
    
}
