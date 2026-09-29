package ru.sberbank.ditsib.corpclient.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import ru.sber.transport.exceptions.dto.Entity;
import ru.sber.transport.exceptions.dto.ExceptionBody;
import ru.sber.transport.exceptions.dto.Problem;
import ru.sber.transport.handlers.RequestExceptionHandler;
import ru.sberbank.ditsib.corpclient.exceptions.DataConflictException;
import ru.sberbank.ditsib.corpclient.exceptions.DataConstrainViolationException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.reflections.util.ConfigurationBuilder.build;

@ControllerAdvice
@Slf4j
public class ExceptionsHandler extends RequestExceptionHandler {
    
    @ExceptionHandler(DataConflictException.class)
    ResponseEntity<ExceptionBody> handleConflict(DataConflictException e, WebRequest request) {
        log.error("ExceptionsHandler: handleConflict: DataConflictException", e);

        var entity = Entity.builder()
                .name(e.getEntity() == null ? "unknown_entity" : e.getEntity().getSimpleName())
                .id(e.getChanged() == null ? "unknown_changed" : e.getChanged().toString())
                .build();
        
        var problem = Problem.builder()
                .value(e.getConflicted() == null ? "unknown_conflicted" : e.getConflicted().toString())
                .field(e.getConflictedField())
                .build();
        
        var body = ExceptionBody.builder()
                .message(e.getType() == null ? "unknown_type" : e.getType().name())
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .entity(entity)
                .problem(problem)
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DataConstrainViolationException.class)
    ResponseEntity<ExceptionBody> handleConstrainViolation(DataConstrainViolationException e, WebRequest request) {
        log.error("ExceptionsHandler: handleConstrain: DataConstrainViolationException", e);

        var entity = Entity.builder()
                .name(e.getEntity() == null ? "unknown_entity" : e.getEntity().getSimpleName())
                .id(e.getChanged() == null ? "unknown_changed" : e.getChanged().toString())
                .build();

        List<Problem> problems = e.getConflicted().entrySet().stream().map(entry ->
                Problem.builder()
                        .value(entry.getValue() == null ? "unknown_conflicted" : entry.getValue().toString())
                        .field(entry.getKey())
                        .build()
                ).toList();

        var builder = ExceptionBody.builder()
                .message(e.getType() == null ? "unknown_type" : e.getType().name())
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .entity(entity);

        problems.forEach(builder::problem);
        var body = builder.build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
