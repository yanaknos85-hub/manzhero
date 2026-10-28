package ru.sber.transport.trips_reports.web.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ru.sber.transport.trips_reports.messaging.mapper.ReportMapper;
import ru.sber.transport.trips_reports.model.Reports;
import ru.sber.transport.trips_reports.web.dto.ReportsDto;
import ru.sber.transport.trips_reports.web.service.ResultProcessor;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class ResultProcessorImpl implements ResultProcessor {

    private final ReportMapper mapper;

    @Override
    public Iterable<ReportsDto> process(Iterable<Reports> result) {
        if (result instanceof Page<Reports> page) {
            return page
                    .map(mapper::toDto);
        } else if (result instanceof Collection<Reports> collection) {
            return collection.parallelStream()
                    .map(mapper::toDto).toList();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public ReportsDto process(Reports result) {
        return mapper.toDto(result);
    }

}
