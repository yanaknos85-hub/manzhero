package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.corpclient.database.dao.AttributeRepository;
import ru.sberbank.ditsib.corpclient.dto.AttributeDto;
import ru.sberbank.ditsib.corpclient.dto.NewAttributeDto;
import ru.sberbank.ditsib.corpclient.mapper.AttributeMapper;
import ru.sberbank.ditsib.corpclient.database.model.Attribute;
import ru.sberbank.ditsib.corpclient.database.model.AttributeStatus;
import ru.sberbank.ditsib.corpclient.service.AttributeService;

import jakarta.validation.constraints.NotNull;
import java.util.*;

@Transactional
@RequiredArgsConstructor
@Component
class AttributeServiceImpl implements AttributeService {
    private final AttributeRepository attributeRepository;

    private final AttributeMapper attributeMapper;

    @Override
    public @NonNull List<Attribute> saveAll(Set<String> nameSet) {
        return attributeRepository.saveAll(attributeMapper.stringToAttribute(nameSet));
    }

    @Override
    public @NonNull List<Attribute> mergeAll(Collection<Attribute> attributeSet) {
        for (Attribute attribute : new HashSet<>(attributeSet)) {
            if (attribute.getId() == null) {
                var attributeByName = attributeRepository.findActiveByName(attribute.getName());
                attributeByName.ifPresent(value -> attribute.setId(value.getId()));
            }
        }
        return attributeRepository.saveAll(attributeSet);
    }

    @Override
    public @NonNull AttributeDto add(NewAttributeDto newData) {
        var attributeByName = attributeRepository.findActiveByName(newData.getName());
        attributeByName.ifPresent(attribute -> {
            throw new DuplicateDataException(Attribute.class, "name", newData.getName());
        });

        var attribute = attributeRepository.save(attributeMapper.stringToAttribute(newData.getName()));
        return attributeMapper.attributeToDTO(attribute);
    }

    @Override
    public @NonNull AttributeDto edit(@NotNull UUID id, @NotNull AttributeDto newData) {
        var editData = attributeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(Attribute.class, id));
        var attributeByName = attributeRepository.findActiveByName(newData.getName());
        attributeByName.ifPresent(attribute -> {
            throw new DuplicateDataException(Attribute.class, "name", newData.getName());
        });

        editData.setName(newData.getName());
        attributeRepository.save(editData);

        return attributeMapper.attributeToDTO(editData);
    }

    @Override
    public void delete(UUID id) {
        var deleteData = attributeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(Attribute.class, id));
        deleteData.setStatus(AttributeStatus.INACTIVE);
        attributeRepository.save(deleteData);
    }

    @Override
    public @NonNull List<AttributeDto> getAll() {
        return attributeRepository.findAll().stream().map(attributeMapper::attributeToDTO).toList();
    }

    @Override
    public @NonNull List<AttributeDto> getActive() {
        return attributeRepository.findAllByStatus(AttributeStatus.ACTIVE).stream()
                .map(attributeMapper::attributeToDTO)
                .toList();
    }
    
    @Override
    public @NonNull Optional<Attribute> get(String name) {
        return attributeRepository.findByName(name);
    }
}
