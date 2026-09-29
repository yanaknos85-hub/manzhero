package ru.sber.transport.limits.web.http.handlers;

import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import ru.sber.transport.exceptions.dto.Constraint;
import ru.sber.transport.exceptions.dto.Entity;
import ru.sber.transport.exceptions.dto.ExceptionBody;
import ru.sber.transport.exceptions.dto.Problem;
import ru.sber.transport.limits.business.exceptions.ClosingStatusNotAllowedException;
import ru.sber.transport.limits.business.exceptions.ClosingUpperLevelNotAllowedException;
import ru.sber.transport.limits.business.exceptions.UpdateNotAllowedException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
class ExceptionHandlerAdvice {

    @ExceptionHandler(ClosingStatusNotAllowedException.class)
    ResponseEntity<ExceptionBody> closingStatusNotAllowedHandler(ClosingStatusNotAllowedException e, WebRequest request) {
        var limitId = e.getLimitId();
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ExceptionBody.builder()
                        .path(((ServletWebRequest)request).getRequest().getRequestURI())
                        .timestamp(OffsetDateTime.now())
                        .message("Cannot to perform operation on limit %s cause it is in status %s. Allowed statuses: %s".formatted(limitId, e.getStatus(), e.getAllowedStatuses()))
                        .entity(Entity.builder().id(limitId).name("Limit").build())
                        .problem(Problem.builder().field("status").value(e.getStatus().name()).constraints(List.of(Constraint.builder().type("UnallowedStatus").value(e.getAllowedStatuses()).build())).build())
                        .build());
    }

    @ExceptionHandler(ClosingUpperLevelNotAllowedException.class)
    ResponseEntity<ExceptionBody> closingUpperNotAllowedHandler(ClosingUpperLevelNotAllowedException e, WebRequest request) {
        var limitId = e.getLimitId();
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ExceptionBody.builder()
                        .path(((ServletWebRequest)request).getRequest().getRequestURI())
                        .timestamp(OffsetDateTime.now())
                        .message("Cannot to perform operation on limit %s cause it is upper-level".formatted(limitId))
                        .entity(Entity.builder().id(limitId).name("Limit").build())
                        .problem(Problem.builder().field("parentId").value(null).constraints(List.of(Constraint.builder().type("UnallowedLevel").build())).build())
                        .build());
    }

    @ExceptionHandler(SpelEvaluationException.class)
    ResponseEntity<ExceptionBody> wrongData(SpelEvaluationException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ExceptionBody.builder()
                        .path(((ServletWebRequest)request).getRequest().getRequestURI())
                        .timestamp(OffsetDateTime.now())
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler(UpdateNotAllowedException.class)
    ResponseEntity<ExceptionBody> updateNotAllowedHandler(UpdateNotAllowedException e, WebRequest request) {
        final var problems = new ArrayList<Problem>();
        for (final var field : e.getFields()) {
            problems.add(Problem
                    .builder()
                    .field(field)
                    .constraints(List.of(Constraint.builder().type("UpdateNotAllowed").value(e.getStatus().name()).build())).build());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ExceptionBody.builder()
                        .path(((ServletWebRequest)request).getRequest().getRequestURI())
                        .timestamp(OffsetDateTime.now())
                        .message(e.getMessage())
                        .problems(problems)
                        .build());
    }
}
