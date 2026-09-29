package ru.sberbank.utils.reflection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Утилита работы с дробными числами")
class DoubleUtilTest {

    @Test
    @DisplayName("Проверка эквивалентности")
    void test_doubleEqualsWithPrecision() {
        assertThat(DoubleUtil.doubleEqualsWithPrecision(1.001, 1.002, 0.01)).isTrue();
    }

}