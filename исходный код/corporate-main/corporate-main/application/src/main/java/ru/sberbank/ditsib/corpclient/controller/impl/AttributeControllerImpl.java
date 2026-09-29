package ru.sberbank.ditsib.corpclient.controller.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.corpclient.controller.AttributeController;
import ru.sberbank.ditsib.corpclient.dto.AttributeDto;
import ru.sberbank.ditsib.corpclient.dto.NewAttributeDto;
import ru.sberbank.ditsib.corpclient.service.AttributeService;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AttributeControllerImpl implements AttributeController {
    private final AttributeService attributeService;

    @Override
    public AttributeDto addAttribute(@Valid NewAttributeDto newData) {
        return attributeService.add(newData);
    }

    @Override
    public void editAttribute(UUID id, @Valid AttributeDto newData) {
        attributeService.edit(id, newData);
    }

    @Override
    public void deleteAttribute(UUID id) {
        attributeService.delete(id);
    }

    @Override
    public Collection<AttributeDto> getAll() {
        return attributeService.getAll();
    }

    @Override
    public Collection<AttributeDto> getActive() {
        return attributeService.getActive();
    }
}
