package ru.sberbank.ditsib.corpclient.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.corpclient.controller.OrganizationGroupController;
import ru.sberbank.ditsib.corpclient.dto.*;
import ru.sberbank.ditsib.corpclient.service.OrganizationGroupService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Transactional
public class OrganizationGroupControllerImpl implements OrganizationGroupController {

    private final OrganizationGroupService organizationGroupService;

    @Override
    public OrganizationGroupResponseDTO add(OrganizationGroupDTO organizationGroupDTO) throws JsonProcessingException {
        return organizationGroupService.add(organizationGroupDTO);
    }

    @Override
    public Page<OrganizationGroupResponseDTO> getAll(OrganizationGroupSearchDTO organizationGroupSearchDTO) {
        return organizationGroupService.getAll(organizationGroupSearchDTO);
    }

    @Override
    public void update(UUID organizationGroupId, OrganizationGroupDTO organizationGroupDTO) {
        organizationGroupService.update(organizationGroupId, organizationGroupDTO);
    }

    @Override
    public void patch(UUID organizationGroupId, List<PatchData<OrganizationGroupPatchFields>> fields) {
        organizationGroupService.patch(organizationGroupId, fields);
    }

    @Override
    public void delete(UUID organizationGroupId) {
        organizationGroupService.delete(organizationGroupId);
    }
}
