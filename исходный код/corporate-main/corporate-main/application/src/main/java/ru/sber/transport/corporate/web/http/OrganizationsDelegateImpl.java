package ru.sber.transport.corporate.web.http;

import jakarta.validation.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.ValidateUnwrappedValue;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.business.Organizations;
import ru.sber.transport.corporate.business.model.Contact;
import ru.sber.transport.corporate.business.model.ContactType;
import ru.sber.transport.corporate.web.http.mappers.OrganizationWebMapper;
import ru.sber.transport.web.api.OrganizationsCommandsApi;
import ru.sber.transport.web.model.NewOrganizationData;
import ru.sber.transport.web.model.OrganizationData;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Component
class OrganizationsDelegateImpl implements OrganizationsCommandsApi {

    private final OrganizationWebMapper mapper;

    private final Organizations organizations;

    @Override
    public ResponseEntity<OrganizationData> add(NewOrganizationData newOrganizationData) {
        var business = mapper.toBusiness(newOrganizationData);
        var violations = new HashSet<ConstraintViolation<?>>();
        var contacts = business.getContacts();
        for (var i = 0; i < contacts.size(); i++) {
            var contact = contacts.get(i);
            var value = contact.getValue();
            if (!value.matches(contact.getType().getRegexp())) {
                violations.add(createViolation(i, contact, value));
            }
        }
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        var added = organizations.add(business);
        var web = mapper.toWeb(added);
        return ResponseEntity.ok(web);
    }

    private ConstraintViolation<?> createViolation(int i, Contact contact, String value) {
        var path = PathImpl.createPathFromString("contacts[%s].value".formatted(i));
        var descriptor = createDescriptor(contact.getType());
        return ConstraintViolationImpl.forBeanValidation(null, null, null, null, Contact.class, contact, null, value, path, descriptor, null);
    }

    private ConstraintDescriptor<?> createDescriptor(ContactType type) {
        var pattern = new Pattern() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Pattern.class;
            }

            @Override
            public String regexp() {
                return type.getRegexp();
            }

            @Override
            public Flag[] flags() {
                return new Flag[0];
            }

            @Override
            public String message() {
                return "";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            public Class<? extends Payload>[] payload() {
                return new Class[0];
            }
        };
        return new ConstraintDescriptor<Pattern>() {

            @Override
            public Pattern getAnnotation() {
                return pattern;
            }

            @Override
            public String getMessageTemplate() {
                return pattern.message();
            }

            @Override
            public Set<Class<?>> getGroups() {
                return Set.of(pattern.groups());
            }

            @Override
            public Set<Class<? extends Payload>> getPayload() {
                return Set.of(pattern.payload());
            }

            @Override
            public ConstraintTarget getValidationAppliesTo() {
                return null;
            }

            @Override
            public List<Class<? extends ConstraintValidator<Pattern, ?>>> getConstraintValidatorClasses() {
                return List.of();
            }

            @Override
            public Map<String, Object> getAttributes() {
                return Map.of();
            }

            @Override
            public Set<ConstraintDescriptor<?>> getComposingConstraints() {
                return Set.of();
            }

            @Override
            public boolean isReportAsSingleViolation() {
                return false;
            }

            @Override
            public ValidateUnwrappedValue getValueUnwrapping() {
                return null;
            }

            @Override
            public <U> U unwrap(Class<U> type) {
                return null;
            }
        };
    }
}
