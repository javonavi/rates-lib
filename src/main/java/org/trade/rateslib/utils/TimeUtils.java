package org.trade.rateslib.utils;

import org.trade.rateslib.model.Timeframe;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author javonavi
 */
public class TimeUtils {

    public static ZonedDateTime convertLocalTime(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return ZonedDateTime.of(time, ZoneId.systemDefault());
    }

    public static long calcDuration(Timeframe timeframe, LocalDateTime time1, LocalDateTime time2) {
        return calcDuration(timeframe, ZonedDateTime.of(time1, ZoneId.systemDefault()), ZonedDateTime.of(time2, ZoneId.systemDefault()));
    }

    public static long calcDuration(Timeframe timeframe, ZonedDateTime time1, ZonedDateTime time2) {
        switch (timeframe) {
            case H1:
                return ChronoUnit.HOURS.between(time1, time2);
            case H4:
                return ChronoUnit.HOURS.between(time1, time2) >> 2;
            case H8:
                return ChronoUnit.HOURS.between(time1, time2) / 3;
            case D1:
                return ChronoUnit.DAYS.between(time1, time2);
            case W1:
                return ChronoUnit.WEEKS.between(time1, time2);
            case MN1:
                return ChronoUnit.MONTHS.between(time1, time2);
            case MN3:
                return ChronoUnit.MONTHS.between(time1, time2) / 3;
            case Y1:
                return ChronoUnit.YEARS.between(time1, time2);

            default:
                throw new RuntimeException("Unhandled timeframe on calcDuration(): " + timeframe);
        }
    }

    public static LocalDateTime plus(LocalDateTime time, Timeframe timeframe) {
        return plus(time, timeframe, 1L);
    }

    public static LocalDateTime plus(LocalDateTime time, Timeframe timeframe, long ratio) {
        switch (timeframe) {
            case H1:
                return time.plusHours(ratio);
            case H4:
                return time.plusHours(ratio << 2);
            case H8:
                return time.plusHours(ratio * 8);
            case D1:
                return time.plusDays(ratio);
            case W1:
                return time.plusWeeks(ratio);
            case MN1:
                return time.plusMonths(ratio);
            case MN3:
                return time.plusMonths(ratio * 3);
            case Y1:
                return time.plusYears(ratio);

            default:
                throw new RuntimeException("Unhandled timeframe on plus(): " + timeframe);
        }
    }

    public static LocalDateTime minus(LocalDateTime time, Timeframe timeframe) {
        return minus(time, timeframe, 1L);
    }

    public static LocalDateTime minus(LocalDateTime time, Timeframe timeframe, long ratio) {
        switch (timeframe) {
            case H1:
                return time.minusHours(ratio);
            case H4:
                return time.minusHours(ratio << 2);
            case H8:
                return time.minusHours(ratio * 8);
            case D1:
                return time.minusDays(ratio);
            case W1:
                return time.minusWeeks(ratio);
            case MN1:
                return time.minusMonths(ratio);
            case MN3:
                return time.minusMonths(ratio * 3);
            case Y1:
                return time.minusYears(ratio);

            default:
                throw new RuntimeException("Unhandled timeframe on minus(): " + timeframe);
        }
    }

    public static double convertBars(Timeframe from, Timeframe to, double bars) {
        double b1 = convertToYear(from, bars);
        return convertFromYear(to, b1);
    }

    private static double convertFromYear(Timeframe timeframe, double bars) {
        switch (timeframe) {
            case Y1:
                return bars;
            case MN1:
                return bars * 12.;
            case W1:
                return bars * 52.142857142857;
            case D1:
                return bars * 365.;
            case H1:
                return bars * (365. * 24.);
            case H4:
                return bars * (365. * 6.);
            case H8:
                return bars * (365. * 3.);

            default:
                throw new RuntimeException("Unexpected timeframeЖ " + timeframe);
        }

    }

    private static double convertToYear(Timeframe timeframe, double bars) {
        switch (timeframe) {
            case Y1:
                return bars;
            case MN1:
                return bars / 12.;
            case W1:
                return bars / 52.142857142857;
            case D1:
                return bars / 365.;
            case H1:
                return bars / (365. * 24.);
            case H4:
                return bars / (365. * 6.);
            case H8:
                return bars / (365. * 3.);

            default:
                throw new RuntimeException("Unexpected timeframeЖ " + timeframe);
        }
    }

    public static LocalDateTime getLatestFinishedBarTime(LocalDateTime time, Timeframe timeframe) {
        switch (timeframe) {
            case M5:
                return time.minusMinutes(time.getMinute() % 5).truncatedTo(ChronoUnit.MINUTES);
            case M15:
                return time.minusMinutes(time.getMinute() % 15).truncatedTo(ChronoUnit.MINUTES);
            case H1:
                return time.truncatedTo(ChronoUnit.HOURS);
            case H4:
                return time.truncatedTo(ChronoUnit.HOURS).minusHours(time.getHour() % 4);
            case H8:
                return time.truncatedTo(ChronoUnit.HOURS).minusHours(time.getHour() % 8);
            case D1:
                return time.truncatedTo(ChronoUnit.DAYS);
            case W1:
                return time.with(DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS);
            case MN1:
                return time.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
            case MN3:
                return time.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS).minusMonths((time.getMonthValue() - 1) % 3);
            case Y1:
                return time.withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);

            default:
                throw new RuntimeException("Unhandled timeframe on getLatestFinishedBarTime(): " + timeframe);
        }

    }
}
