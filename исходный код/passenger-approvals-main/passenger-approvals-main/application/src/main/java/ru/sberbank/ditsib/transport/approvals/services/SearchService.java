package ru.sberbank.ditsib.transport.approvals.services;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.dto.ApprovalJournalDto;
import ru.sberbank.ditsib.transport.approvals.dto.Type;
import ru.sberbank.ditsib.transport.approvals.dto.params.EmployeeSearchParams;

import java.util.List;
import java.util.UUID;

/**
 * Сервис поиска согласований.
 */
public interface SearchService {

    /**
     * Поиск количества согласований.
     *
     * @param statuses список статусов для поиска.
     * @param userId идентификатор вошедшего пользователя.
     * @param types типы согласований. Если типы не указаны, поиск осуществляется по всем типам.
     *
     * @return количество согласований.
     */
    long searchCount(
            @NonNull List<Status> statuses,
            UUID userId,
            List<Type> types
    );

    /**
     * Поиск согласований.
     *
     * @param userId идентификатор вошедшего пользователя.
     * @param statuses список статусов для поиска.
     * @param types типы согласований. Если типы не указаны, поиск осуществляется по всем типам.
     * @param transportType типы транспорт. Если тип не указан, поиск осуществляется по типам в доступных департаментах.
     * @param pageable параметры пагинации.
     * @param employeeSearchParams параметры поиска по пассажиру.
     *
     * @return {@link Page<ApprovalJournalDto>} страница согласований для журнала.
     */
    Page<ApprovalJournalDto> searchForJournal(UUID userId,
                                              List<Status> statuses,
                                              List<Type> types,
                                              String transportType,
                                              Pageable pageable,
                                              EmployeeSearchParams employeeSearchParams);

    void saveApprovalJournalForSharedRideJoinApproval(SharedRideJoinApproval savedApproval);

    void saveApprovalJournalForTripRequestApproval(TripRequestApproval savedApproval);

    void saveApprovalJournalForFinalTripApproval(FinalTripApproval savedApproval);

    void saveApprovalJournalForUpdateTripRequestApproval(UpdateTripRequestApproval savedApproval);
}
