package ru.sberbank.ditsib.corpclient.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.corpclient.database.model.Organization;

import java.util.UUID;

/**
 * Exception throws if department with given ID not found,
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Organization not found")
public class OrganizationNotFoundException extends EntityNotFoundException {
    
    /**
     * Create a new exception.
     *
     * @param id ID of organization.
     */
    public OrganizationNotFoundException(UUID id) {
        super(Organization.class, id);
    }
}
