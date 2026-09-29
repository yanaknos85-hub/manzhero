package ru.sberbank.ditsib.corpclient.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.messages.corporate.avro.DelegateData;
import ru.sberbank.ditsib.corpclient.database.model.DelegateRecord;
import ru.sberbank.ditsib.corpclient.dto.DelegateRecordDTO;
import ru.sberbank.ditsib.corpclient.dto.GetDelegateRecordDTO;
import ru.sberbank.ditsib.transport.messaging.messages.DelegateMessage;

/**
 * Маппер делегатов.
 */
@Mapper(uses = { EmployeeMapper.class })
public interface DelegateMapper {

    /**
     * Преобразование записи о делегате в сообщение.
     *
     * @param delegateRecord запись о делегате.
     * @return сообщение.
     */
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "id", source = "delegateRecord.id")
    @Mapping(target = "supervisorId", source = "delegateRecord.supervisor.id")
    @Mapping(target = "delegateId", source = "delegateRecord.delegate.id")
    @Mapping(target = "startDate", source = "delegateRecord.startDate")
    @Mapping(target = "endDate", source = "delegateRecord.endDate")
    @Mapping(target = "transportTypeId", source = "delegateRecord.transportType.id")
    DelegateMessage toMessage(DelegateRecord delegateRecord);

    /**
     * Преобразование записи о делегате в сообщение.
     *
     * @param delegateRecord запись о делегате.
     * @return сообщение.
     */
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "id", source = "delegateRecord.id")
    @Mapping(target = "supervisorId", source = "delegateRecord.supervisor.id")
    @Mapping(target = "delegateId", source = "delegateRecord.delegate.id")
    @Mapping(target = "startDate", source = "delegateRecord.startDate")
    @Mapping(target = "endDate", source = "delegateRecord.endDate")
    @Mapping(target = "transportType", source = "delegateRecord.transportType.name")
    DelegateData toMessageAvro(DelegateRecord delegateRecord);

    /**
     * Сборка сообщения об удалении делегата.
     *
     * @param deleted признак удаления. Полномочия прекращены
     * @return сообщение.
     */

    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "id", source = "delegateRecord.id")
    @Mapping(target = "supervisorId", source = "delegateRecord.supervisor.id")
    @Mapping(target = "delegateId", source = "delegateRecord.delegate.id")
    @Mapping(target = "startDate", source = "delegateRecord.startDate")
    @Mapping(target = "endDate", source = "delegateRecord.endDate")
    @Mapping(target = "transportTypeId", source = "delegateRecord.transportType.id")
    DelegateMessage toMessage(DelegateRecord delegateRecord, boolean deleted);

    /**
     * Сборка сообщения об удалении делегата.
     *
     * @param deleted признак удаления. Полномочия прекращены
     * @return сообщение.
     */

    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "id", source = "delegateRecord.id")
    @Mapping(target = "supervisorId", source = "delegateRecord.supervisor.id")
    @Mapping(target = "delegateId", source = "delegateRecord.delegate.id")
    @Mapping(target = "startDate", source = "delegateRecord.startDate")
    @Mapping(target = "endDate", source = "delegateRecord.endDate")
    @Mapping(target = "transportType", source = "delegateRecord.transportType.name")
    DelegateData toMessageAvro(DelegateRecord delegateRecord, boolean deleted);

    @Mapping(target = "supervisor", source = "supervisorId")
    @Mapping(target = "delegate", source = "delegateId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    DelegateRecord dtoToDelegate(DelegateRecordDTO source);

    @Mapping(source = "supervisor.id", target = "supervisorId")
    @Mapping(source = "delegate.id", target = "delegateId")
    @Mapping(source = "delegate", target = "delegateEmployee")
    @Mapping(source = "status", target = "status")
    GetDelegateRecordDTO delegateRecordToDTO(DelegateRecord source);
}
