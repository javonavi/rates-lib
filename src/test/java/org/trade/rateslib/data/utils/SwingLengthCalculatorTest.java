package org.trade.rateslib.data.utils;

import org.junit.jupiter.api.Test;
import org.trade.rateslib.utils.SwingLengthCalculator;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Ivanov Andrey
 * @since 03.01.2023
 */
public class SwingLengthCalculatorTest {

    @Test
    public void test1() {
        LocalDateTime time1 = LocalDateTime.of(2021, 3, 1, 0, 0, 0);
        LocalDateTime time2 = LocalDateTime.of(2021, 5, 23, 0, 0, 0);
        double val = SwingLengthCalculator.calcTimeBetween("MN1", time1, time2);
        System.out.println(val);
        assertTrue(val > 2);
    }

}
