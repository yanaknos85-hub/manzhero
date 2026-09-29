package ru.sber.transport.humanreadableid.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.sber.transport.humanreadableid.dao.AbstractRepository;
import ru.sber.transport.humanreadableid.model.interfaces.Prefix;
import ru.sber.transport.humanreadableid.service.CompanySQService;
import ru.sber.transport.humanreadableid.service.HumanReadbaleIdFormatter;
import ru.sber.transport.humanreadableid.service.SQGenerator;

import java.util.stream.LongStream;

/**
 * Implementation of service for sequence generate
 */
@Service
@Validated
@RequiredArgsConstructor
@Slf4j
@Qualifier("sQGeneratorHR")
public class SQGeneratorImpl implements SQGenerator {

    private final CompanySQService companySQService;
    private final AbstractRepository<?> abstractRepository;
    private final HumanReadbaleIdFormatter humanReadbaleIdFormatter;


    @Override
    public String getNextId(@NonNull Prefix prefix, @NonNull Long organizationId) {
        return reserve(prefix, organizationId, 1).iterator().next();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public Iterable<String> reserve(@NonNull Prefix prefix, @NonNull Long organizationId, int count) {
        var lastId = companySQService.getOrCreateCompanySQ(prefix, organizationId, count);
        return LongStream.range(lastId + 1, lastId + count + 1)
                .mapToObj(id -> humanReadbaleIdFormatter.format(prefix, organizationId, id))
                .sorted()
                .toList();
    }

}
