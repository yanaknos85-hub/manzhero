package ru.sber.transport.limits.web.http.resolvers.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.limits.web.http.resolvers.ImportDataPreparer;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.dto.file.LimitDataFileDto;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка механизма подготовки к импорту")
class ImportDataPreparerImplTest {

    private final ApplicationContext context = mock(ApplicationContext.class);

    private final OrganizationDataImporter importer = mock(OrganizationDataImporter.class);

    private final ImportDataPreparer preparer = new ImportDataPreparerImpl(context);

    private final UUID organizationId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        when(context.getBean(OrganizationDataImporter.class)).thenReturn(importer);
    }

    @AfterEach
    void tearDown() {
        preparer.clear(organizationId);
    }

    @Test
    @DisplayName("Проверка дубликата подготовщика")
    void test_preparer_duplicate() {
        var authorId = UUID.randomUUID();

        preparer.prepare(organizationId, authorId);

        assertThatThrownBy(() -> preparer.prepare(organizationId, authorId))
                .isInstanceOf(DuplicateDataException.class)
                .hasFieldOrPropertyWithValue("entityName", "Import limit")
                .hasFieldOrPropertyWithValue("values", Map.of("organization_id", organizationId));
    }

    @Test
    @DisplayName("Проверка услуг - разные типы")
    void test_head_different_services() {
        var authorId = UUID.randomUUID();
        var source = Instancio.of(LimitDataFileDto.class)
                .set(Select.field(LimitDataFileDto::getTransportType), TransportTypeEnum.TAXI.name())
                .set(Select.field(LimitDataFileDto::getLimitType), "DEP")
                .set(Select.field(LimitDataFileDto::getBalance), BigDecimal.valueOf(50))
                .set(Select.field(LimitDataFileDto::getSum), BigDecimal.valueOf(100))
                .create();
        var department = Instancio.create(Department.class);

        when(importer.getDepartment(source.getDepartmentCode())).thenReturn(department);

        preparer.prepare(organizationId, authorId);

        assertThatThrownBy(() -> preparer.add(organizationId, source))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Файл импорта содержит разные типы услуг");
    }

    @Test
    @DisplayName("Проверка услуг - разные годы")
    void test_head_different_years() {
        var authorId = UUID.randomUUID();
        var source = Instancio.of(LimitDataFileDto.class)
                .set(Select.field(LimitDataFileDto::getTransportType), TransportTypeEnum.TAXI.name())
                .set(Select.field(LimitDataFileDto::getLimitType), "DEP")
                .set(Select.field(LimitDataFileDto::getBalance), BigDecimal.valueOf(50))
                .set(Select.field(LimitDataFileDto::getSum), BigDecimal.valueOf(100))
                .create();
        var department = Instancio.create(Department.class);

        when(importer.getDepartment(source.getDepartmentCode())).thenReturn(department);
        when(importer.getServiceType()).thenReturn("PASSENGER");

        preparer.prepare(organizationId, authorId);

        assertThatThrownBy(() -> preparer.add(organizationId, source))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Файл импорта содержит разные года");
    }

    @Test
    @DisplayName("Проверка услуг - превышение суммы")
    void test_head_sum_overdue() {
        var authorId = UUID.randomUUID();
        var source = Instancio.of(LimitDataFileDto.class)
                .set(Select.field(LimitDataFileDto::getTransportType), TransportTypeEnum.TAXI.name())
                .set(Select.field(LimitDataFileDto::getLimitType), "DEP")
                .set(Select.field(LimitDataFileDto::getBalance), BigDecimal.valueOf(100))
                .set(Select.field(LimitDataFileDto::getSum), BigDecimal.valueOf(50))
                .create();
        var department = Instancio.create(Department.class);

        when(importer.getDepartment(source.getDepartmentCode())).thenReturn(department);
        when(importer.getServiceType()).thenReturn("PASSENGER");

        preparer.prepare(organizationId, authorId);

        assertThatThrownBy(() -> preparer.add(organizationId, source))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Файл импорта содержит записи с балансом больше суммы");
    }

    @Test
    @DisplayName("Проверка услуг - дубликаты")
    void test_head_duplicates() {
        var authorId = UUID.randomUUID();
        var source = Instancio.of(LimitDataFileDto.class)
                .set(Select.field(LimitDataFileDto::getTransportType), TransportTypeEnum.TAXI.name())
                .set(Select.field(LimitDataFileDto::getLimitType), "DEP")
                .set(Select.field(LimitDataFileDto::getBalance), BigDecimal.valueOf(50))
                .set(Select.field(LimitDataFileDto::getSum), BigDecimal.valueOf(100))
                .create();
        var department = Instancio.create(Department.class);

        when(importer.getDepartment(source.getDepartmentCode())).thenReturn(department);
        when(importer.getServiceType()).thenReturn("PASSENGER");
        when(importer.getYear()).thenReturn(source.getYear());

        preparer.prepare(organizationId, authorId);

        assertThatThrownBy(() -> preparer.add(organizationId, source))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Файл импорта содержит дублирующиеся записи");
    }

    @Test
    @DisplayName("Проверка услуг - кэш не инициализирован")
    void test_not_initialized() {
        var source = Instancio.create(LimitDataFileDto.class);

        assertThatThrownBy(() -> preparer.add(organizationId, source))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Preparer must be initialized before using. Please call prepare(UUID) to initialize");
    }

    @Test
    @DisplayName("Получение года")
    void test_getYear() {
        var year = Instancio.create(Integer.class);

        when(importer.getYear()).thenReturn(year);

        preparer.prepare(organizationId, UUID.randomUUID());

        assertThat(preparer.getYear(organizationId)).isEqualTo(year);
    }

}