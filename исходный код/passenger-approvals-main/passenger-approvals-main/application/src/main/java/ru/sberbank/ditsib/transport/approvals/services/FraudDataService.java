package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.database.model.Approval;
import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestMessage;

import java.util.List;

public interface FraudDataService {

    /**
     * Обновление данных по фроду для заявки на согласование
     *
     * @param approval ссылка на согласование
     * @param fraudData список данных по фроду
     */
    void updateFraudDataForApproval(Approval approval,
                                    List<RequestMessage.Fraud> fraudData);
}
