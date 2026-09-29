package ru.sber.transport.corporate.web.handlers;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.sber.transport.exceptions.dto.ExceptionBody;

@Slf4j
@Component
@ControllerAdvice
@RequiredArgsConstructor
public class ControllerExceptionHandlerImpl extends ResponseEntityExceptionHandler {

    /**
     * Исключение гео-провайдера.
     *
     * @param exception исключение.
     * @param request запрос.
     * @return ответ.
     */
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ExceptionBody> handleUnsupportedOperationException(UnsupportedOperationException exception, WebRequest request) {
        log.info("Операция не поддерживается");
        log.debug(exception.getMessage(), exception);
        var body = ExceptionBody.builder()
            .message(exception.getMessage())
            .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
    
}
