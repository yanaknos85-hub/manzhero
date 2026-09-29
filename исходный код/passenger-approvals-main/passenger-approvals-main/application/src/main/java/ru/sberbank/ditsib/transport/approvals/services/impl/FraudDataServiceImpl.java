package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.approvals.database.dao.FraudDataRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.Approval;
import ru.sberbank.ditsib.transport.approvals.mappers.FraudDataMapper;
import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestMessage;
import ru.sberbank.ditsib.transport.approvals.services.FraudDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FraudDataServiceImpl implements FraudDataService {

    private final FraudDataMapper fraudDataMapper;
    private final FraudDataRepository repository;

    @Transactional
    @Override
    public void updateFraudDataForApproval(Approval tripRequestApprovalEntity, List<RequestMessage.Fraud> fraudData) {
        if (CollectionUtils.isEmpty(fraudData)) {
            return;
        }

        if (Objects.nonNull(tripRequestApprovalEntity.getId()) && CollectionUtils.isNotEmpty(tripRequestApprovalEntity.getFraudData())) {
            tripRequestApprovalEntity.getFraudData().clear();
            repository.deleteByRequestId(fraudData.getFirst().requestId());
        }

        final var fraudEntities = fraudDataMapper.toEntities(fraudData);

        if (fraudEntities != null) {
            for (var fraudEntity : fraudEntities) {
                fraudEntity.setApproval(tripRequestApprovalEntity);
            }
        }

        tripRequestApprovalEntity.setFraudData(CollectionUtils.isNotEmpty(fraudEntities) ? fraudEntities : new ArrayList<>());
    }
}
