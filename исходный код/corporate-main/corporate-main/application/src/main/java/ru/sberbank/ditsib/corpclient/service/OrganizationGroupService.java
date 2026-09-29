package ru.sberbank.ditsib.corpclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;
import ru.sberbank.ditsib.corpclient.dto.*;

import java.util.List;
import java.util.UUID;

/**
 * Сервис по работе с группами организаций
 */
public interface OrganizationGroupService {

    /**
     * Добавление группы организаций
     * @param organizationGroupDTO данные группы организаций
     * @return Данные добавленной группы организаций
     */
    OrganizationGroupResponseDTO add(OrganizationGroupDTO organizationGroupDTO) throws JsonProcessingException;

    /**
     * Получение групп организаций
     * @param organizationGroupSearchDTO данные для поиска групп организаций
     * @return Данные групп организаций
     */
    Page<OrganizationGroupResponseDTO> getAll(OrganizationGroupSearchDTO organizationGroupSearchDTO);

    /**
     * Редактирование группы организаций
     * @param organizationGroupId ID группы организаций
     * @param organizationGroupDTO данные группы организаций для редактирования
     */
    void update(UUID organizationGroupId, OrganizationGroupDTO organizationGroupDTO);

    /**
     * Частичное редактирование группы организаций
     * @param organizationGroupId ID группы организаций
     * @param fields данные группы организаций для редактирования
     */
    void patch(UUID organizationGroupId, List<PatchData<OrganizationGroupPatchFields>> fields);

    /**
     * Удаление группы организаций
     * @param organizationGroupId ID группы организаций
     */
    void delete(UUID organizationGroupId);

}
