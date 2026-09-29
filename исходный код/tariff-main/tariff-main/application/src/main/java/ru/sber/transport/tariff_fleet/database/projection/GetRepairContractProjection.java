package ru.sber.transport.tariff_fleet.database.projection;

import java.time.LocalDate;
import java.util.UUID;

public interface GetRepairContractProjection {
    
    UUID getContractorId();
    
    String getContractorName();
    
    String getOrganizationName();
    
    UUID getId();
    
    String getNumber();
    
    String getUvhd();
    
    Long getAmount();
    
    LocalDate getStart();
    
    LocalDate getEnd();
    
    boolean getActive();
    
    void setContractorId(UUID value);
    
    void setContractorName(String value);
    
    void setOrganizationName(String value);
    
    void setId(UUID value);
    
    void setNumber(String value);
    
    void setUvhd(String value);
    
    void setAmount(Long value);
    
    void setStart(LocalDate value);
    
    void setEnd(LocalDate value);
    
    void setActive(Boolean value);
}
