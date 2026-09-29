package ru.sber.transport.integrations.service;

import ru.sber.transport.integrations.dto.ContractorInfoRequest;

public interface CarLocationService {
    
    void getOrdersLocation(ContractorInfoRequest contractorInfoRequest);
}
