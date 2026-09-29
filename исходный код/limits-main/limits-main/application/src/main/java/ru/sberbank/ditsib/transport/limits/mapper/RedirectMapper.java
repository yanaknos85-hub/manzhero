package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sberbank.ditsib.transport.constants.limits.LimitStatus;
import ru.sberbank.ditsib.transport.limits.dto.DepLimitSharingDTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.DepLimitPrimaryV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitResharingV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v3.CreateOrganizationReserveDTO;
import ru.sberbank.ditsib.transport.limits.dto.v3.CreateOrganizationReserveResponseDTO;
import ru.sberbank.ditsib.transport.limits.dto.v3.DepartmentReserveDTO;
import ru.sberbank.ditsib.transport.limits.dto.v3.ReShareDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mapper
public interface RedirectMapper {

    @Mapping(target = "service", source = "dto.limitServiceType")
    @Mapping(target = "sum", source = "dto.sum")
    @Mapping(target = "organizationId", source = "organizationId")
    CreateOrganizationReserveDTO toCreateOrganizationReserveDTO (DepLimitPrimaryV2DTO dto, UUID organizationId);

    @Mapping(target = "limitStatus", expression = "java(mapStatus(dto.getStatus()))")
    @Mapping(target = "limitServiceType", source = "dto.service")
    @Mapping(target = "limitSharingType", constant = "MONTHLY")
    GetLimitV2DTO toGetLimitV2DTO(CreateOrganizationReserveResponseDTO dto);

    @Mapping(target = "limitStatus", expression = "java(mapStatus(dto.getStatus()))")
    @Mapping(target = "limitServiceType", source = "dto.service")
    void updateGetLimitV2DTO(@MappingTarget GetLimitV2DTO v2DTO, CreateOrganizationReserveResponseDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "departmentId", source = "departmentId")
    @Mapping(target = "transportTypes", expression = "java(fillMap(dto, isPublic))")
    DepartmentReserveDTO toDepartmentReserveDto(LimitResharingV2DTO dto, UUID departmentId, boolean isPublic);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "departmentId", source = "dto.targetDepartment")
    @Mapping(target = "transportTypes", expression = "java(fillMap(dto, isPublic))")
    DepartmentReserveDTO toDepartmentReserveDto(ReShareDTO dto, boolean isPublic);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "departmentId", source = "departmentId")
    @Mapping(target = "transportTypes", expression = "java(fillMap(dto, isPublic))")
    DepartmentReserveDTO toDepartmentReserveDto(DepLimitSharingDTO dto, UUID departmentId, boolean isPublic);

    default LimitStatus mapStatus(CreateOrganizationReserveResponseDTO.Status status){
        return switch (status){
            case DRAFT -> LimitStatus.PLANNING;
            case USING -> LimitStatus.SHARED;
            case CLOSED -> LimitStatus.CLOSED;
        };
    }

    default CreateOrganizationReserveResponseDTO.Status mapStatus(LimitStatus status){
        return switch (status){
            case PLANNING -> CreateOrganizationReserveResponseDTO.Status.DRAFT;
            case SHARED -> CreateOrganizationReserveResponseDTO.Status.USING;
            case CLOSED -> CreateOrganizationReserveResponseDTO.Status.CLOSED;
        };
    }

    default Map<String, DepartmentReserveDTO.TransportTypeData> fillMap(LimitResharingV2DTO dto, boolean isPublic){
        return Map.of(dto.targetTransportType().name(), new DepartmentReserveDTO.TransportTypeData(dto.sum(), isPublic));
    }

    default Map<String, DepartmentReserveDTO.TransportTypeData> fillMap(ReShareDTO dto, boolean isPublic){
        return Map.of(dto.getTransportType().name(), new DepartmentReserveDTO.TransportTypeData(dto.getSum(), isPublic));
    }

    default Map<String, DepartmentReserveDTO.TransportTypeData> fillMap(DepLimitSharingDTO dto, boolean isPublic){
        var map = new HashMap<String, DepartmentReserveDTO.TransportTypeData>();
        dto.getSharingPerTransportList().forEach(obj -> map.put(obj.getTransportType().name(), new DepartmentReserveDTO.TransportTypeData(obj.getSum(), isPublic)));
        return map;
    }

}
