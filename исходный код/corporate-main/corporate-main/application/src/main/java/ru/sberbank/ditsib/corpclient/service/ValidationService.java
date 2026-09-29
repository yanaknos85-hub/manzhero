package ru.sberbank.ditsib.corpclient.service;

import java.util.List;
import java.util.UUID;

public interface ValidationService {

    void validateDepartmentIds(UUID organizationId, List<UUID> departmentIds);
}
