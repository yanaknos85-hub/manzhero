package ru.sber.transport.tariff_fleet.model;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;
import ru.sber.transport.tariff_fleet.constant.ContractorType;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.constant.ServiceType;

import java.util.UUID;

@Builder
public record ContractorFilter(
    /**
     * Флаг активности
     */
    boolean active,
    
    /**
     * Тип интеграции
     */
    ContractorType contractorType,
    
    /**
     * Тип сервиса
     */
    @NotNull
    ServiceType serviceType,
    
    /**
     * Тип документа
     */
    @NotNull
    DocumentType documentType,
    
    /**
     * Только свои организации
     */
    boolean selfOnly,
    
    /**
     * Идентификатор организации
     */
    UUID organizationId) { }
