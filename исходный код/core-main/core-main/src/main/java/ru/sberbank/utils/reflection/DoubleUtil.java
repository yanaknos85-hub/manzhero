package ru.sberbank.utils.reflection;

import lombok.experimental.UtilityClass;

/**
 * Утилита для работы со сравнениями Double
 */
@UtilityClass
public class DoubleUtil {

    /**
     * Общепринятая погрешность для проверки дробгых чисел.
     */
    public static final double EPSILON = 1E-6;

    /**
     * Равенство чисел с погрешностью.
     *
     * @param a первое число для проверки.
     * @param b второе число для проверки.
     * @param precision погрешность.
     * @return <code>true</code>, если числа приблизительно равны.
     */
    public boolean doubleEqualsWithPrecision(double a, double b, double precision) {
        return Math.abs(a - b) <= precision;
    }

}
