package ru.sberbank.ditsib.transport.limits.service.impl.spec;

import jakarta.persistence.criteria.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import ru.sberbank.ditsib.transport.limits.dto.LimitSearchDTO;
import ru.sberbank.ditsib.transport.limits.model.basic.Department_;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee_;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization_;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit_;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

/**
 * Спецификация для поиска лимитов.
 */
@RequiredArgsConstructor
public class LimitSearchSpec implements Specification<Limit> {

    private final UUID organizationId;

    private final LimitSearchDTO limitSearchDTO;

    @Override
    public Predicate toPredicate(@NonNull Root<Limit> root, CriteriaQuery<?> query, @NonNull CriteriaBuilder builder) {
        assert query != null : "Query is not initialized";
        query.distinct(true);

        Predicate predicate;
        if (organizationId != null) {
            var organizationJoin = root.join(Limit_.organization);
            predicate = builder.equal(organizationJoin.get(Organization_.id), organizationId);
        } else {
            predicate = builder.isTrue(builder.literal(true));
        }

        predicate = appendEqual(predicate, builder, root.get(Limit_.id), limitSearchDTO.getLimitId());
        if (limitSearchDTO.getParentLimitId() != null) {
            var parentLimitJoin = root.join(Limit_.parent);
            predicate = appendEqual(predicate, builder, parentLimitJoin.get(Limit_.id), limitSearchDTO.getParentLimitId());
        }
        predicate = appendEqual(predicate, builder, root.get(Limit_.year), limitSearchDTO.getYear());
        predicate = appendEqual(predicate, builder, root.get(Limit_.limitType), limitSearchDTO.getLimitType());
        predicate = appendEqual(predicate, builder, root.get(Limit_.limitServiceType), limitSearchDTO.getLimitServiceType());
        predicate = appendEqual(predicate, builder, root.get(Limit_.limitStatus), limitSearchDTO.getLimitStatus());
        predicate = appendLike(predicate, builder, root.get(Limit_.humanReadableId), limitSearchDTO.getHumanReadableLimitId());
        predicate = filterParentDepartment(root, builder, predicate);
        predicate = filterOwner(root, builder, predicate);

        return predicate;
    }

    private Predicate filterParentDepartment(@NotNull Root<Limit> root, @NotNull CriteriaBuilder builder, Predicate predicate) {
        var parentDepartment = limitSearchDTO.getParentDepartment();
        if (!StringUtils.isBlank(parentDepartment)) {  // name or code
            parentDepartment = "%%%s%%".formatted(parentDepartment.toLowerCase());
            var parentDepartmentJoin = root.join(Limit_.parentDepartment);
            var codePredicate = builder.like(builder.lower(parentDepartmentJoin.get(Department_.code)), parentDepartment);
            var namePredicate = builder.like(builder.lower(parentDepartmentJoin.get(Department_.departmentName)), parentDepartment);

            var parentDepartmentPredicate = builder.or(codePredicate, namePredicate);
            predicate = builder.and(predicate, parentDepartmentPredicate);
        }
        return predicate;
    }

    private Predicate filterOwner(@NotNull Root<Limit> root, @NotNull CriteriaBuilder builder, Predicate predicate) {
        var owner = limitSearchDTO.getLimitOwner();
        if (!StringUtils.isBlank(owner)) {   // fio or tabel-code
            owner = "%%%s%%".formatted(owner.replace(" ", "")
                    .replace(".", "")
                    .replace("-", "")
                    .toLowerCase());
            Join<Limit, Employee> limitOwnerJoin = root.join(Limit_.limitOwner);

            var firstNamePath = limitOwnerJoin.get(Employee_.firstName);
            var lastNamePath = limitOwnerJoin.get(Employee_.lastName);
            var patronymicPath = limitOwnerJoin.get(Employee_.patronymic);

            BiFunction<Expression<String>, String, Expression<String>> replace = (source, delimiter) -> builder.function("REPLACE", String.class, source, builder.literal(delimiter), builder.literal(""));
            UnaryOperator<Expression<String>> replaceAll = (source) -> replace.apply(replace.apply(replace.apply(source, " "), "-"), ".");

            var firstNamePredicate = builder.like(builder.lower(firstNamePath), owner);
            var lastNamePredicate = builder.like(builder.lower(lastNamePath), owner);
            var lastNameFirstNamePredicate = builder.like(builder.lower(builder.concat(lastNamePath, firstNamePath)), owner);
            var lastNameFirstNamePatronymicPredicate = builder.like(replaceAll.apply(builder.lower(builder.concat(builder.concat(lastNamePath, builder.substring(firstNamePath, 1, 1)), builder.substring(patronymicPath, 1, 1)))), owner);
            var lastNameFirstNamePatronymicInitialsPredicate = builder.like(builder.lower(builder.concat(builder.concat(lastNamePath, firstNamePath), patronymicPath)), owner);
            var firstNamePatronymicPredicate = builder.like(replaceAll.apply(builder.lower(builder.concat(firstNamePath, builder.substring(patronymicPath, 1, 1)))), owner);

            var personnelNumberPredicate =
                    builder.like(builder.lower(limitOwnerJoin.get(Employee_.personnelNumber)), owner);

            var limitOwnerPredicate = builder.or(firstNamePredicate,
                    lastNamePredicate,
                    lastNameFirstNamePredicate,
                    lastNameFirstNamePatronymicPredicate,
                    lastNameFirstNamePatronymicInitialsPredicate,
                    firstNamePatronymicPredicate,
                    personnelNumberPredicate);
            predicate = builder.and(predicate, limitOwnerPredicate);
        }
        return predicate;
    }

    private Predicate appendLike(Predicate predicate, CriteriaBuilder builder, Path<String> stringPath, String value) {
        if (!StringUtils.isBlank(value)) {
            predicate = builder.and(predicate, builder.like(stringPath, "%" + value + "%"));
        }
        return predicate;
    }

    private <V> Predicate appendEqual(Predicate predicate, CriteriaBuilder builder, Path<V> valuePath, V value) {
        if (value != null) {
            predicate = builder.and(predicate, builder.equal(valuePath, value));
        }
        return predicate;
    }

}
