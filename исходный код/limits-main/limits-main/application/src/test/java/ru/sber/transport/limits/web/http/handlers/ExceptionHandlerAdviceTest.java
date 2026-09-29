package ru.sber.transport.limits.web.http.handlers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.request.ServletWebRequest;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.exceptions.dto.Constraint;
import ru.sber.transport.exceptions.dto.Problem;
import ru.sber.transport.limits.business.exceptions.UpdateNotAllowedException;
import ru.sber.transport.limits.business.model.Status;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка обработчиков исключений")
class ExceptionHandlerAdviceTest {

    private final ExceptionHandlerAdvice exceptionHandlerAdvice = new ExceptionHandlerAdvice();

    @Test
    @DisplayName("Проверка обработки ошибок при неправильных данных")
    void test_wrongData() {
        var spelMessage = SpelMessage.ARRAY_INDEX_OUT_OF_BOUNDS;
        var exception = new SpelEvaluationException(spelMessage);
        var request = new ServletWebRequest(MockMvcRequestBuilders.get("/limits").buildRequest(new MockServletContext()));

        var response = exceptionHandlerAdvice.wrongData(exception, request);

        assertThat(response).isNotNull();

        assertSoftly(it -> {
           it.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
           it.assertThat(response.getBody().getPath()).isEqualTo("/limits");
           it.assertThat(response.getBody().getTimestamp()).isNotNull();
           it.assertThat(response.getBody().getMessage()).isEqualTo("EL1024E: The array has '{0}' elements, index '{1}' is invalid");
        });
    }

    @Test
    @DisplayName("Проверка обработки ошибок при невозможности обновления")
    void test_updateNotAllowed() {
        var exception = new UpdateNotAllowedException(Status.CLOSED, "field1", "field2");
        var request = new ServletWebRequest(MockMvcRequestBuilders.get("/limits").buildRequest(new MockServletContext()));

        var response = exceptionHandlerAdvice.updateNotAllowedHandler(exception, request);

        assertSoftly(it -> {
            it.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            it.assertThat(response.getBody().getPath()).isEqualTo("/limits");
            it.assertThat(response.getBody().getTimestamp()).isNotNull();
            it.assertThat(response.getBody().getMessage()).isEqualTo("Update not allowed for status: CLOSED for fields: field1, field2");
            it.assertThat(response.getBody().getProblems()).hasSize(2);
            it.assertThat(response.getBody().getProblems().stream().map(Problem::getField).toList()).containsExactlyInAnyOrder("field1", "field2");
            it.assertThat(response.getBody().getProblems().stream().map(Problem::getConstraints).flatMap(Collection::stream).map(Constraint::getType).toList()).containsExactlyInAnyOrder("UpdateNotAllowed", "UpdateNotAllowed");
            it.assertThat(response.getBody().getProblems().stream().map(Problem::getConstraints).flatMap(Collection::stream).map(Constraint::getValue).toList()).containsExactlyInAnyOrder("CLOSED", "CLOSED");
        });
    }

}