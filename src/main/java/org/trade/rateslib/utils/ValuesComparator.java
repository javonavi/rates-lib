package org.trade.rateslib.utils;

/**
 * Сравнивает числа с учетом погрешности
 *
 * @author Ivanov Andrey
 * @since 08.01.2024
 */
public class ValuesComparator {

    private static final double DEFAULT_MAX_ERROR = 0.03;

    public static boolean isEqual(double value1, double value2, double maxError) {
        if (value2 == 0) {
            if (value1 <= maxError) {
                return true;
            }
            return false;
        }
        double k = value1 / value2;
        if (k >= 2) {
            return false;
        }
        if (k >= 1) {
            k -= 1;
        } else {
            k = 1 - k;
        }
        return k <= maxError;
    }

    public static boolean isEqual(double value1, double value2) {
        return isEqual(value1, value2, DEFAULT_MAX_ERROR);
    }
}
