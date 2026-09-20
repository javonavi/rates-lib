package org.trade.rateslib.utils;

import org.junit.jupiter.api.Test;
import org.trade.rateslib.model.Timeframe;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TimeConverterTest {

    @Test
    public void testConvertMn1ToMn3() {
        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0),
                TimeConverter.convertToUpperTimeframe(Timeframe.MN1, Timeframe.MN3, LocalDateTime.of(2024, 2, 14, 10, 30)));
        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0),
                TimeConverter.convertToUpperTimeframe(Timeframe.MN1, Timeframe.MN3, LocalDateTime.of(2024, 3, 31, 23, 59)));
        assertEquals(LocalDateTime.of(2024, 4, 1, 0, 0),
                TimeConverter.convertToUpperTimeframe(Timeframe.MN1, Timeframe.MN3, LocalDateTime.of(2024, 4, 1, 0, 0)));
        assertEquals(LocalDateTime.of(2024, 4, 1, 0, 0),
                TimeConverter.convertToUpperTimeframe(Timeframe.MN1, Timeframe.MN3, LocalDateTime.of(2024, 6, 15, 0, 0)));
        assertEquals(LocalDateTime.of(2024, 7, 1, 0, 0),
                TimeConverter.convertToUpperTimeframe(Timeframe.MN1, Timeframe.MN3, LocalDateTime.of(2024, 9, 30, 12, 0)));
        assertEquals(LocalDateTime.of(2024, 10, 1, 0, 0),
                TimeConverter.convertToUpperTimeframe(Timeframe.MN1, Timeframe.MN3, LocalDateTime.of(2024, 12, 5, 0, 0)));
    }

    @Test
    public void testConvertMn1ToY1() {
        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0),
                TimeConverter.convertToUpperTimeframe(Timeframe.MN1, Timeframe.Y1, LocalDateTime.of(2024, 7, 14, 10, 30)));
        assertEquals(LocalDateTime.of(2023, 1, 1, 0, 0),
                TimeConverter.convertToUpperTimeframe(Timeframe.MN1, Timeframe.Y1, LocalDateTime.of(2023, 1, 1, 0, 0)));
    }

    @Test
    public void testConvertMn3ToY1() {
        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0),
                TimeConverter.convertToUpperTimeframe(Timeframe.MN3, Timeframe.Y1, LocalDateTime.of(2024, 10, 1, 0, 0)));
    }

    @Test
    public void testConvertH1ToH4() {
        assertEquals(LocalDateTime.of(2024, 5, 28, 16, 0),
                TimeConverter.convertToUpperTimeframe(Timeframe.H1, Timeframe.H4, LocalDateTime.of(2024, 5, 28, 19, 39)));
        assertEquals(LocalDateTime.of(2024, 5, 28, 20, 0),
                TimeConverter.convertToUpperTimeframe(Timeframe.H1, Timeframe.H4, LocalDateTime.of(2024, 5, 28, 20, 0)));
        assertEquals(LocalDateTime.of(2024, 5, 28, 0, 0),
                TimeConverter.convertToUpperTimeframe(Timeframe.H1, Timeframe.H4, LocalDateTime.of(2024, 5, 28, 3, 15)));
    }

    @Test
    public void testConvertH1ToD1() {
        assertEquals(LocalDateTime.of(2024, 5, 28, 0, 0),
                TimeConverter.convertToUpperTimeframe(Timeframe.H1, Timeframe.D1, LocalDateTime.of(2024, 5, 28, 19, 39)));
    }

    @Test
    public void testConvertD1ToW1() {
        assertEquals(LocalDateTime.of(2024, 5, 27, 0, 0),
                TimeConverter.convertToUpperTimeframe(Timeframe.D1, Timeframe.W1, LocalDateTime.of(2024, 5, 28, 0, 0)));
    }

    @Test
    public void testConvertD1ToMn1() {
        assertEquals(LocalDateTime.of(2024, 5, 1, 0, 0),
                TimeConverter.convertToUpperTimeframe(Timeframe.D1, Timeframe.MN1, LocalDateTime.of(2024, 5, 28, 0, 0)));
    }

    @Test
    public void testConvertSameTimeframe() {
        assertEquals(LocalDateTime.of(2024, 5, 28, 19, 39),
                TimeConverter.convertToUpperTimeframe(Timeframe.H1, Timeframe.H1, LocalDateTime.of(2024, 5, 28, 19, 39)));
    }

    @Test
    public void testConvertToLowerTimeframeReturnsNull() {
        assertNull(TimeConverter.convertToUpperTimeframe(Timeframe.D1, Timeframe.H1, LocalDateTime.of(2024, 5, 28, 0, 0)));
    }
}
