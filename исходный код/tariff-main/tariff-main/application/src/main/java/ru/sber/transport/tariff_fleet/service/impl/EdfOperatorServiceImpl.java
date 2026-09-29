package ru.sber.transport.tariff_fleet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.tariff_fleet.database.dao.EdfOperatorRepository;
import ru.sber.transport.tariff_fleet.database.model.EdfOperator;
import ru.sber.transport.tariff_fleet.dto.EdfOperatorDto;
import ru.sber.transport.tariff_fleet.mapper.EdfOperatorMapper;
import ru.sber.transport.tariff_fleet.service.EdfOperatorService;

import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class EdfOperatorServiceImpl implements EdfOperatorService {
    private final EdfOperatorRepository edfOperatorRepository;
    private final EdfOperatorMapper edfOperatorMapper;
    
    @Override
    @Transactional(readOnly = true)
    public Optional<EdfOperator> getById(String id) {
        return edfOperatorRepository.findById(id);
    }

    @Override
    public Set<EdfOperatorDto> getAllActive() {
        return edfOperatorMapper.setEdfOperatorToSetEdfOperatorDto(edfOperatorRepository.findAllByActiveIsTrue());
    }
}
