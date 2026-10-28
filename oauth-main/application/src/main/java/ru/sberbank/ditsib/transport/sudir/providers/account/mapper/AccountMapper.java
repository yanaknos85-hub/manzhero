package ru.sberbank.ditsib.transport.sudir.providers.account.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.sber.transport.database.sudir.tables.records.AccountRecord;
import ru.sber.transport.database.sudir.tables.records.RoleRecord;
import ru.sberbank.ditsib.transport.sudir.business.model.Account;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Маппер УЗ из БД в бизнес.
 */
@Mapper
public interface AccountMapper {

    /**
     * Convert database to business.
     *
     * @param source source entity,
     * @param roles set of roles.
     * @return business entity.
     */
    default Account toBusiness(AccountRecord source, Set<RoleRecord> roles) {
        return Account.builder()
            .id(source.getId())
            .personnelNumber(source.getPersonnelNumber())
            .active(source.getActive())
            .roles(roles.stream().collect(Collectors.toMap(RoleRecord::getCode, RoleRecord::getDataMaster)))
            .build();
    }

    /**
     * Update data.
     *
     * @param target object to update.
     * @param source object with source data for updating.
     */
    void toModel(
        @MappingTarget AccountRecord target,
        Account source
    );
}
