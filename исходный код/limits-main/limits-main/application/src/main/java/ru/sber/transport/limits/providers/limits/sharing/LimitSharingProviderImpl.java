package ru.sber.transport.limits.providers.limits.sharing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.limits.tables.Sharings;
import ru.sber.transport.database.limits.tables.records.SharingsRecord;
import ru.sber.transport.limits.business.model.LimitSharing;
import ru.sber.transport.limits.business.model.LimitSharingPerPeriod;
import ru.sber.transport.limits.business.providers.LimitSharingPerPeriodProvider;
import ru.sber.transport.limits.business.providers.LimitSharingProvider;
import ru.sber.transport.limits.providers.limits.sharing.mappers.LimitSharingDatabaseMapper;
import ru.sber.transport.limits.web.providers.SharingsProvider;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@Transactional
@RequiredArgsConstructor
public class LimitSharingProviderImpl implements LimitSharingProvider, SharingsProvider, JooqRepository<ru.sber.transport.database.limits.tables.Sharings, SharingsRecord, UUID> {

    private final LimitSharingDatabaseMapper mapper;

    private final LimitSharingPerPeriodProvider limitSharingPerPeriodProvider;

    @Override
    public List<LimitSharing> getAll(UUID limitId) {
        return context()
                .selectFrom(table())
                .where(table().LIMIT_ID.eq(limitId))
                .fetchInto(SharingsRecord.class).stream().map(mapper::toBusiness).toList();
    }

    @Override
    public void save(LimitSharing source) {
        var found = findById(source.getId()).orElseGet(SharingsRecord::new);
        if (found.getId() == null) {
            found.setId(UUID.randomUUID());
        }
        mapper.update(found, source);
        save(found);
    }

    @Override
    public ru.sber.transport.database.limits.tables.Sharings table() {
        return Sharings.SHARINGS;
    }

    @Override
    public Map<UUID, List<LimitSharing>> get(List<UUID> ids) {
        final var month = LocalDate.now().getMonth();
        final var result = context().select()
                .from(table())
                .where(table().LIMIT_ID.in(ids))
                .fetch()
                .parallelStream()
                .collect(Collectors.toMap(it -> it.get(table().LIMIT_ID), it -> List.of(mapper.toBusiness(it.into(SharingsRecord.class))), (l, r) -> Stream.concat(l.stream(), r.stream()).toList()));
        result.values().parallelStream().forEach(items -> {
            final var elements = items.parallelStream().collect(Collectors.toMap(LimitSharing::getId, Function.identity()));
            final var sharings = limitSharingPerPeriodProvider.get(elements.keySet(), month).parallelStream().collect(Collectors.toMap(LimitSharingPerPeriod::getLimitSharingId, Function.identity()));
            items.parallelStream().forEach(item -> {
                item.setPeriods(Stream.of(sharings.get(item.getId())).filter(Objects::nonNull).toList());
            });
        });
        return result;
    }
}
