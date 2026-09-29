package ru.sberbank.ditsib.transport.limits.service.impl;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.limits.constants.LimitSpendingStatus;
import ru.sberbank.ditsib.transport.limits.dao.LimitSpendingRepository;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.LimitSpendingService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LimitSpendingServiceImpl implements LimitSpendingService {

    private final LimitSpendingRepository limitSpendingRepository;

    private final EntityManager entityManager;

    @Override
    @Transactional
    public LimitSpending add(final LimitSpending limitSpending) {
        limitSpending.setReservationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        LimitSpending result;
        try {
            result = limitSpendingRepository.save(limitSpending);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return result;
    }

    @Override
    @Transactional
    public void save(LimitSpending limitSpending) {
        try {
            limitSpendingRepository.save(limitSpending);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void delete(LimitSpending limitSpending) {
        try {
            limitSpendingRepository.delete(limitSpending);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<LimitSpending> get(UUID id) {
        return limitSpendingRepository.findById(id);
    }

    @Override
    public List<LimitSpending> getAll() {
        return limitSpendingRepository.findAll();
    }

    @Override
    public LimitSpending getByRequestId(UUID requestId) {
        List<LimitSpending> list = limitSpendingRepository.findByRequestId(requestId);
        if (list.size() == 1) {
            return list.getFirst();
        }
        return null;
    }

    @Override
    public List<LimitSpending> getByLimitSharingPerPeriod(LimitSharingPerPeriod limitSharingPerPeriod) {
        return limitSpendingRepository.findByLimitSharingPerPeriod(limitSharingPerPeriod);
    }

    @Override
    public List<LimitSpending> getByLimit(UUID limitId) {
        return limitSpendingRepository.findByLimit(limitId);
    }

    @Override
    public Map<UUID, Period> getSpendingPeriods(Collection<UUID> spendingIds) {
        Specification<LimitSpending> spec = (root, query, cb) -> {
            root.fetch(LimitSpending_.limitSharingPerPeriod);
            return root.get(LimitSpending_.id).in(spendingIds);
        };
        return limitSpendingRepository.findAll(spec).stream().collect(Collectors.toMap(LimitSpending::getId, it -> it.getLimitSharingPerPeriod().getPeriod()));
    }

    @Override
    public Map<UUID, UUID> getSpendingSharings(Collection<UUID> spendingIds) {
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(AbstractMap.SimpleEntry.class);
        var root = query.from(LimitSpending.class);
        var sharing = root.join(LimitSpending_.limitSharingPerPeriod).join(LimitSharingPerPeriod_.limitSharing);
        var predicate = root.get(LimitSpending_.id).in(spendingIds);
        var multiselect = query.multiselect(root.get(LimitSpending_.id).alias("spending"), sharing.get(LimitSharing_.id).alias("sharing"));
        var emQuery = entityManager.createQuery(multiselect.where(predicate));
        return emQuery.getResultStream().collect(Collectors.toMap(it -> UUID.fromString(String.valueOf(it.getKey())), it -> UUID.fromString(String.valueOf(it.getValue()))));
    }

    @Override
    public Map<UUID, List<LimitSpending>> getByLimitAndStatusNot(Set<UUID> limitIds, LimitSpendingStatus limitSpendingStatus) {
        Specification<LimitSpending> spec = (root, query, cb) -> {
            var limits = root.join(LimitSpending_.limitSharingPerPeriod).join(LimitSharingPerPeriod_.limitSharing).join(LimitSharing_.limit);
            root.fetch(LimitSpending_.limitSharingPerPeriod).fetch(LimitSharingPerPeriod_.limitSharing).fetch(LimitSharing_.limit);
            return cb.and(limits.get(Limit_.id).in(limitIds), cb.notEqual(root.get(LimitSpending_.status), limitSpendingStatus));
        };
        return limitSpendingRepository.findAll(spec).stream()
            .collect(Collectors.toMap(it -> it.getLimitSharingPerPeriod().getLimitSharing().getLimit().getId(), List::of, (l, r) -> Stream.concat(l.stream(), r.stream()).toList()));
    }
}
