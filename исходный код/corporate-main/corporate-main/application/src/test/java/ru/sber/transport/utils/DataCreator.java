package ru.sber.transport.utils;

import org.instancio.Instancio;
import ru.sber.transport.corporate.business.model.Active;
import ru.sber.transport.corporate.business.model.ContactType;
import ru.sber.transport.corporate.business.model.Organization;
import ru.sber.transport.database.corporate.enums.ActiveStatus;
import ru.sber.transport.database.corporate.enums.StructureType;
import ru.sber.transport.database.corporate.tables.records.*;
import ru.sber.transport.database.corporate_approvals.tables.records.ApprovalsRecord;
import ru.sber.transport.web.model.NewOrganizationData;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface DataCreator {

    default Organization createOrganization(NewOrganizationData source) {
        var organization = new Organization();
        organization.setName(source.getOfficialName());
        organization.setAddress(source.getAddress());
        organization.setMsrn(source.getMsrn());
        organization.setCode(source.getOrganizationCode());
        organization.setTid(source.getTid());
        organization.setSyncId(source.getEasupId());
        return organization;
    }

    default OrganizationRecord createOrganizationRecord(int index) {
        var organization = new OrganizationRecord();
        organization.setId(UUID.randomUUID());
        organization.setOfficialName("Name " + index);
        organization.setAddress("Address " + index);
        organization.setMsrn("MSRN " + index);
        organization.setOrganizationCode(index);
        organization.setTin("TIN " + index);
        organization.setSyncId("Easup " + index);
        return organization;
    }

    default DepartmentRecord createDepartmentRecord(int index, UUID organizationId) {
        return createDepartmentRecord(index, organizationId, null);
    }

    default DepartmentRecord createDepartmentRecord(int index, UUID organizationId, UUID parentId) {
        var department = new DepartmentRecord();
        department.setId(UUID.randomUUID());
        department.setHumanreadableid("HRI " + index);
        department.setUpdateTime(OffsetDateTime.now());
        department.setSyncId("Easup " + index);
        department.setFilialFlag(index % 2 == 0);
        department.setCode("Code " + index);
        department.setName("Name " + index);
        department.setOrganizationId(organizationId);
        department.setOrgStructureType(StructureType.values()[index % StructureType.values().length]);
        department.setParentId(parentId);
        return department;
    }

    default PositionRecord createPositionRecord(int index, UUID organizationId) {
        var position = new PositionRecord();
        position.setId(UUID.randomUUID());
        position.setHumanreadableid("HRI " + index);
        position.setUpdateTime(OffsetDateTime.now());
        position.setSyncId("Easup " + index);
        position.setName("Name " + index);
        position.setOrganizationId(organizationId);
        position.setOrgStructureType(StructureType.values()[index % StructureType.values().length]);
        return position;
    }

    default EmployeeRecord createEmployeeRecord(int index, UUID organizationId, UUID departmentId, UUID positionId) {
        var employee = new EmployeeRecord();
        employee.setId(UUID.randomUUID());
        employee.setStatus(ActiveStatus.values()[index % ActiveStatus.values().length]);
        employee.setConsent(index % 2 == 0);
        employee.setCostCenter("Cost center " + index);
        employee.setOrganizationId(organizationId);
        employee.setHumanreadableid("HRI " + index);
        employee.setEmail("Email " + index);
        employee.setExternalEmail("External email " + index);
        employee.setFirstName("First name " + index);
        employee.setLastName("Last name " + index);
        employee.setMarriageCertificateId("Marriage " + index);
        employee.setFireDate(LocalDate.now());
        employee.setMobilePhone("Phone " + index);
        employee.setPatronymic("Patronymic " + index);
        employee.setPersonnelNumber("Personnel number " + index);
        employee.setRoom("Room " + index);
        employee.setDepartmentId(departmentId);
        employee.setPositionId(positionId);
        employee.setOrgStructureType(StructureType.values()[index % StructureType.values().length]);
        return employee;
    }

    default Organization createOrganization(Organization source) {
        var organization = new Organization();
        organization.setName(source.getName());
        organization.setAddress(source.getAddress());
        organization.setContacts(source.getContacts());
        organization.setMsrn(source.getMsrn());
        organization.setCode(source.getCode());
        organization.setTid(source.getTid());
        organization.setSyncId(source.getSyncId());
        if (source.getId() == null) {
            organization.setId(UUID.randomUUID());
        } else {
            organization.setId(source.getId());
        }
        if (source.getStatus() == null) {
            organization.setStatus(Instancio.create(Active.class));
        } else {
            organization.setStatus(source.getStatus());
        }
        organization.setDigitId(source.getDigitId());
        return organization;
    }

    default AttributeRecord createAttributeRecord(int index) {
        var attribute = new AttributeRecord();
        attribute.setId(UUID.randomUUID());
        attribute.setName("Name " + index);
        attribute.setStatus(Instancio.create(ActiveStatus.class));
        return attribute;
    }

    default ApprovalsRecord createApprovalRecord(UUID employeeId) {
        var approval = new ApprovalsRecord();
        approval.setEmployeeId(employeeId);
        approval.setActionId(UUID.randomUUID());
        return approval;
    }

    default ru.sber.transport.corporate.business.model.Contact createContact(ru.sber.transport.corporate.business.model.Contact source) {
        var contact = new ru.sber.transport.corporate.business.model.Contact();
        contact.setType(ContactType.valueOf(source.getType().name()));
        contact.setValue(source.getValue());
        contact.setId(source.getId());
        return contact;
    }

}
