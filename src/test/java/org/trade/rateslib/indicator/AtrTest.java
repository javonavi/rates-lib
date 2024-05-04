package org.trade.rateslib.indicator;

import org.junit.jupiter.api.Test;
import org.trade.rateslib.model.Rate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Ivanov Andrey
 * @since 04.05.2024
 */
public class AtrTest {

    @Test
    public void test1() {
        List<Rate> rates = Arrays.asList(
            compileRate(21.51, 21.51, 21.51),
                compileRate(21.95, 20.22, 21.61),
                compileRate(22.25, 21.10, 20.83),
                compileRate(21.50, 20.34, 22.65),
                compileRate(23.25, 22.13, 22.41),
                compileRate(23.03, 21.87, 22.67),
                compileRate(23.34, 22.18, 23.05),
                compileRate(23.66, 22.57, 23.31),
                compileRate(23.97, 22.80, 23.68),
                compileRate(24.29, 23.15, 23.97),
                compileRate(24.60, 23.45, 24.31),
                compileRate(24.92, 23.76, 24.60),
                compileRate(25.23, 24.09, 24.89),
                compileRate(25.55, 24.39, 25.20));

        Atr atr = new Atr();
        List<Double> result = atr.calc(rates, 12);
        assertEquals(2, result.size());
    }

    private Rate compileRate(double high,
                             double low,
                             double close) {
        return Rate.builder()
                .withOpen(close)
                .withHigh(high)
                .withLow(low)
                .withClose(close)
                .withTimeframe("W1")
                .withTime(LocalDateTime.now())
                .build();
    }
}
