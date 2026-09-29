package ru.sber.transport.tariff_fleet.database.projection;

import java.time.LocalDate;
import java.util.UUID;

public interface GetEwbContractProjection {
    UUID getId();
    
    String getNumber();
    
    LocalDate getStart();
    
    LocalDate getEnd();
    
    boolean isActive();
    
    String getOrganizationName();
    
    String getInspectionType();
    
    Long getAmount();
    
    void setId(UUID id);
    
    void setNumber(String value);
    
    void setStart(LocalDate value);
    
    void setEnd(LocalDate value);
    
    void setActive(boolean value);
    
    void setOrganizationName(String value);
    
    void setInspectionType(String value);
    
    void setAmount(Long value);
}