package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.dto.TripApproveDTO;

import java.util.Collection;
import java.util.UUID;

/**
 * Сервис для общей логики контроллеров согласований
 **/
public interface ApproveControllerService {
    <A extends TripApproveDTO> Collection<A> fillTripPurpose(Collection<A> dtoList);
    
    /**
     * Заполнить лимиты для дто согласований (поля sumLimit, restOfLimit)
     *
     * @param dtoList список дто согласований
     * @param departmentId id департамента сотрудника
     * @param token токен авторизованного пользователя
     * @param skipApprovalsWithNoLimitSharing не выдавать ошибку, при отсутствии распределения лимита для согласований
     *
     * @return список дто с заполненными лимитами
     */
    <A extends TripApproveDTO> Collection<A> fillLimits(Collection<A> dtoList, UUID departmentId, String token, boolean skipApprovalsWithNoLimitSharing);
}
