package ru.sber.transport.tariff_fleet.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.sber.transport.tariff_fleet.constant.ContractorType;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.constant.ServiceType;

import static org.junit.jupiter.api.Assertions.*;

class ContractorFilterTest {

    @Test
    void testConstructorAndGetters() {
        var filter = new ContractorFilter(true, ContractorType.API, ServiceType.AUTOSERVICE, DocumentType.REPAIR_AND_MAINTENANCE, false, java.util.UUID.randomUUID());

        Assertions.assertTrue(filter.active());          // Проверка флага активности
        assertEquals(ContractorType.API, filter.contractorType());   // Проверка типа интегратора
        assertEquals(ServiceType.AUTOSERVICE, filter.serviceType());           // Проверка типа сервиса
        assertEquals(DocumentType.REPAIR_AND_MAINTENANCE, filter.documentType());       // Проверка типа документа
        assertFalse(filter.selfOnly());        // Проверка "Только свои организации"
        assertNotNull(filter.organizationId());      // Проверка наличия ID организации
    }

    // Тест равенства объектов с одинаковым содержимым
    @Test
    void testEqualsSameContent() {
        var uuid = java.util.UUID.randomUUID();
        var firstFilter = new ContractorFilter(true, ContractorType.API, ServiceType.AUTOSERVICE, DocumentType.REPAIR_AND_MAINTENANCE, true, uuid);
        var secondFilter = new ContractorFilter(true, ContractorType.API, ServiceType.AUTOSERVICE, DocumentType.REPAIR_AND_MAINTENANCE, true, uuid);

        assertEquals(firstFilter, secondFilter); // Должны быть равны
    }

    // Тест неравенства объектов с разным содержимым
    @Test
    void testEqualsDifferentContent() {
        var uuidFirst = java.util.UUID.randomUUID();
        var uuidSecond = java.util.UUID.randomUUID();

        var firstFilter = new ContractorFilter(false, ContractorType.API, ServiceType.AUTOSERVICE, DocumentType.REPAIR_AND_MAINTENANCE, true, uuidFirst);
        var secondFilter = new ContractorFilter(true, ContractorType.AUTOSERVICE_EXTERNAL, ServiceType.EMPLOYEE_TRANSPORTATION, DocumentType.REPAIR_AND_MAINTENANCE, false, uuidSecond);

        assertNotEquals(firstFilter, secondFilter);
    }

    // Тест hashCode
    @Test
    void testHashCode() {
        var uuid = java.util.UUID.randomUUID();
        var firstFilter = new ContractorFilter(true, ContractorType.API, ServiceType.AUTOSERVICE, DocumentType.REPAIR_AND_MAINTENANCE, true, uuid);
        var secondFilter = new ContractorFilter(true, ContractorType.API, ServiceType.AUTOSERVICE, DocumentType.REPAIR_AND_MAINTENANCE, true, uuid);

        assertEquals(firstFilter.hashCode(), secondFilter.hashCode());
    }
}