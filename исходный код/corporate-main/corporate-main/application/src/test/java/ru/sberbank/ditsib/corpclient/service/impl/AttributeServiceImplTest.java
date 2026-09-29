package ru.sberbank.ditsib.corpclient.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.corpclient.database.dao.AttributeRepository;
import ru.sberbank.ditsib.corpclient.database.model.Attribute;
import ru.sberbank.ditsib.corpclient.database.model.AttributeStatus;
import ru.sberbank.ditsib.corpclient.dto.AttributeDto;
import ru.sberbank.ditsib.corpclient.dto.NewAttributeDto;
import ru.sberbank.ditsib.corpclient.mapper.AttributeMapper;
import ru.sberbank.ditsib.corpclient.mapper.AttributeMapperImpl;
import ru.sberbank.ditsib.corpclient.service.AttributeService;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка сервиса атрибутов")
class AttributeServiceImplTest {

    private final AttributeRepository repository = mock(AttributeRepository.class);

    private final AttributeMapper attributeMapper = new AttributeMapperImpl();

    private final AttributeService service = new AttributeServiceImpl(repository, attributeMapper);

    @Test
    @DisplayName("Сохранение всего")
    void test_saveAll() {
        var attributes = Instancio.createSet(Attribute.class);
        var data = attributes.stream().map(Attribute::getName).collect(Collectors.toSet());

        when(repository.saveAll(any())).then(inv -> inv.getArgument(0, LinkedHashSet.class).stream().map(s -> new Attribute(UUID.randomUUID(), String.valueOf(s), AttributeStatus.ACTIVE, List.of())).collect(Collectors.toList()));

        var saved = service.saveAll(data);

        assertThat(saved).hasSameSizeAs(attributes);
    }

    @Test
    @DisplayName("Смержить списки")
    void test_mergeAll() {
        var attributes = Instancio.ofList(Attribute.class)
            .ignore(Select.field(Attribute::getId))
            .create();
        var attribute = Instancio.create(Attribute.class);

        when(repository.findActiveByName(attributes.iterator().next().getName())).thenReturn(Optional.of(attribute));
        when(repository.saveAll(any())).then(inv -> inv.getArgument(0));

        var merged = service.mergeAll(attributes);

        assertThat(merged).hasSize(attributes.size());
        assertThat(merged.get(0).getId()).isNotNull().isEqualTo(attribute.getId());
    }

    @Test
    @DisplayName("Сохранение. Конфликт")
    void test_save_conflict() {
        var newData = Instancio.create(NewAttributeDto.class);
        var attribute = Instancio.create(Attribute.class);

        when(repository.findActiveByName(newData.getName())).thenReturn(Optional.of(attribute));

        assertThatThrownBy(() -> service.add(newData))
            .isInstanceOf(DuplicateDataException.class)
            .hasFieldOrPropertyWithValue("entityName", "Attribute")
            .hasFieldOrPropertyWithValue("values", Map.of("name", newData.getName()));
    }

    @Test
    @DisplayName("Изменение. Нет элемента")
    void test_edit_notFound() {
        var id = UUID.randomUUID();
        var newData = Instancio.create(AttributeDto.class);

        assertThatThrownBy(() -> service.edit(id, newData))
            .isInstanceOf(EntityNotFoundException.class)
            .hasFieldOrPropertyWithValue("entityId", id)
            .hasFieldOrPropertyWithValue("entityName", "Attribute");
    }

    @Test
    @DisplayName("Изменение. Конфликт")
    void test_edit_conflict() {
        var id = UUID.randomUUID();
        var newData = Instancio.create(AttributeDto.class);
        var attribute = Instancio.create(Attribute.class);

        when(repository.findById(id)).thenReturn(Optional.of(attribute));
        when(repository.findActiveByName(newData.getName())).thenReturn(Optional.of(attribute));

        assertThatThrownBy(() -> service.edit(id, newData))
            .isInstanceOf(DuplicateDataException.class)
            .hasFieldOrPropertyWithValue("values", Map.of("name", newData.getName()))
            .hasFieldOrPropertyWithValue("entityName", "Attribute");
    }

    @Test
    @DisplayName("Изменение")
    void test_edit() {
        var id = UUID.randomUUID();
        var newData = Instancio.create(AttributeDto.class);
        var attribute = Instancio.create(Attribute.class);

        when(repository.findById(id)).thenReturn(Optional.of(attribute));

        var actual = service.edit(id, newData);

        assertSoftly(soft -> {
            soft.assertThat(actual.getId()).isEqualTo(attribute.getId());
            soft.assertThat(actual.getName()).isEqualTo(newData.getName());
        });

        var captor = ArgumentCaptor.forClass(Attribute.class);

        verify(repository).save(captor.capture());


        assertSoftly(soft -> {
            soft.assertThat(captor.getValue().getId()).isEqualTo(attribute.getId());
            soft.assertThat(captor.getValue().getName()).isEqualTo(newData.getName());
        });
    }

    @Test
    @DisplayName("Получение данных")
    void test_get() {
        var name = Instancio.create(String.class);
        var attribute = Instancio.create(Attribute.class);

        when(repository.findByName(name)).thenReturn(Optional.of(attribute));

        var actual = service.get(name);

        assertThat(actual).isPresent();
        assertSoftly(soft -> {
            soft.assertThat(actual.get().getId()).isEqualTo(attribute.getId());
            soft.assertThat(actual.get().getName()).isEqualTo(attribute.getName());
            soft.assertThat(actual.get().getStatus()).isEqualTo(attribute.getStatus());
            soft.assertThat(actual.get().getEmployees()).hasSameSizeAs(attribute.getEmployees());
        });
    }

}