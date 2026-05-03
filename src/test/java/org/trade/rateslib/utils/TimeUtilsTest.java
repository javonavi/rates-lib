package org.trade.rateslib.utils;

import org.junit.jupiter.api.Test;
import org.trade.rateslib.model.Timeframe;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeUtilsTest {

    @Test
    public void testGetLatestFinishedBarTime() {
        assertEquals(
                LocalDateTime.of(2024, 5, 1, 0, 0, 0),
                TimeUtils.getLatestFinishedBarTime(LocalDateTime.of(2024, 5, 28, 21, 39, 0), Timeframe.MN1));

        assertEquals(
                LocalDateTime.of(2024, 5, 27, 0, 0, 0),
                TimeUtils.getLatestFinishedBarTime(LocalDateTime.of(2024, 5, 28, 21, 39, 0), Timeframe.W1));

        assertEquals(
                LocalDateTime.of(2024, 5, 28, 0, 0, 0),
                TimeUtils.getLatestFinishedBarTime(LocalDateTime.of(2024, 5, 28, 21, 39, 0), Timeframe.D1));

        assertEquals(
                LocalDateTime.of(2024, 5, 28, 20, 0, 0),
                TimeUtils.getLatestFinishedBarTime(LocalDateTime.of(2024, 5, 28, 21, 39, 0), Timeframe.H4));

        assertEquals(
                LocalDateTime.of(2024, 5, 28, 21, 0, 0),
                TimeUtils.getLatestFinishedBarTime(LocalDateTime.of(2024, 5, 28, 21, 39, 0), Timeframe.H1));

        assertEquals(
                LocalDateTime.of(2024, 5, 28, 21, 30, 0),
                TimeUtils.getLatestFinishedBarTime(LocalDateTime.of(2024, 5, 28, 21, 39, 0), Timeframe.M15));

        assertEquals(
                LocalDateTime.of(2024, 5, 28, 21, 35, 0),
                TimeUtils.getLatestFinishedBarTime(LocalDateTime.of(2024, 5, 28, 21, 39, 0), Timeframe.M5));
    }

    @Test
    public void testCalcDuration() {
        assertEquals(19, TimeUtils.calcDuration(Timeframe.W1,
                LocalDate.of(2025, 4, 7).atTime(0, 0),
                LocalDate.of(2025, 8, 18).atTime(0, 0)));
    }

}
