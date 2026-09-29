package ru.sber.transport.corporate.providers.human_readable;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.Table;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.corporate.business.providers.HumanReadableProvider;
import ru.sber.transport.database.corporate.tables.CompanySq;
import ru.sber.transport.database.corporate.tables.Organization;
import ru.sber.transport.database.corporate.tables.records.CompanySqRecord;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Transactional
@Slf4j
public abstract class BaseHumanReadableProvider<T extends Table<?>, E> implements HumanReadableProvider<E>, JooqRepository<CompanySq, CompanySqRecord, UUID> {

    @Override
    public synchronized List<String> getNext(@NotNull UUID organizationId, int count) {
        log.debug("Get {} human readable IDs for {} at organization {}", count, prefix(), organizationId);
        var prefix = prefix();
        var organization = Organization.ORGANIZATION;
        var digitId = context()
                .select(organization.DIGIT_ID)
                .from(organization)
                .where(organization.ID.eq(organizationId))
                .fetchOptional(organization.DIGIT_ID)
                .orElseThrow(() -> new EntityNotFoundException(Organization.class, organizationId));
        var formattedPrefix = "%s-%04d-".formatted(prefix, digitId);

        log.debug("Digit ID for organization {} is {}", prefix(), digitId);

        var result = context()
                .select(table().ID, table().SQ)
                .from(table())
                .where(table().ORGDIGITID.eq(digitId))
                .and(table().PREFIX.eq(prefix))
                .fetchOptional();

        var nextId = result
                .map(it -> updateGenerator(digitId, it.get(table().SQ), formattedPrefix, count))
                .orElseGet(() -> createGenerator(digitId, prefix, count));

        var humanReadableId = getPossibleId(formattedPrefix, nextId);
        if (log.isDebugEnabled()) {
            log.debug("Generated human readable id is {}", humanReadableId);
        } else {
            log.debug("Generated {} human readable ids", humanReadableId.size());
        }
        return humanReadableId;
    }

    @NotNull
    private List<Integer> updateGenerator(Long digitId, BigInteger current, String formattedPrefix, int count) {
        log.debug("Human readable generator {} for organization {} found. Checking", prefix(), digitId);
        var field = humanReadableField(entityTable());
        var result = new ArrayList<Integer>();
        List<Integer> nextIds;
        do {
            var value = current.intValue();
            nextIds = new ArrayList<>(IntStream.range(0, count).mapToObj(i -> value + i + 1).toList());
            var possibleId = getPossibleId(formattedPrefix, nextIds);
            var foundIds = new ArrayList<>(context().select(field).from(entityTable()).where(field.in(possibleId))
                    .fetch(0, String.class)
                    .stream().map(it -> it.split("-")[2])
                    .map(Integer::parseInt)
                    .toList());
            result.addAll(nextIds.parallelStream().filter(it -> !foundIds.contains(it)).toList());
            current = BigInteger.valueOf(Collections.max(nextIds) + 1L);
            nextIds = foundIds;
            count = foundIds.size();
        } while (!nextIds.isEmpty());

        context().update(table())
                .set(table().DT_MODIFY, OffsetDateTime.now())
                .set(table().SQ, BigInteger.valueOf(result.stream().max(Integer::compareTo).orElseThrow()))
                .execute();
        return result;
    }

    private List<Integer> createGenerator(Long digitId, String prefix, int count) {
        var nextId = BigInteger.valueOf(count);
        log.debug("Human readable generator {} for organization {} not found. Creating a new one", prefix(), digitId);
        context().insertInto(table())
                .set(table().ID, UUID.randomUUID())
                .set(table().PREFIX, prefix)
                .set(table().ORGDIGITID, digitId)
                .set(table().SQ, nextId)
                .set(table().DT_MODIFY, OffsetDateTime.now())
                .set(table().DT_INSERT, OffsetDateTime.now())
                .execute();
        return IntStream.range(1, count + 1).boxed().toList();
    }

    private List<String> getPossibleId(String formattedPrefix, List<Integer> digitsId) {
        return digitsId.stream().map(i -> "%s%08d".formatted(formattedPrefix, i)).toList();
    }

    protected abstract String prefix();

    protected abstract T entityTable();

    protected abstract Field<String> humanReadableField(T table);

    @Override
    public CompanySq table() {
        return CompanySq.COMPANY_SQ;
    }

}
