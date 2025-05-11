package org.trade.rateslib.model;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public enum Timeframe {
    M5("M5", 5, "M15", null, cnt -> TimeframeDuration.of(Period.ZERO, Duration.ofMinutes(cnt * 5))),
    M15("M15", 15, "H1", "M5", cnt -> TimeframeDuration.of(Period.ZERO, Duration.ofMinutes(cnt * 15))),
    H1("H1", 60, "H4", "M15", cnt -> TimeframeDuration.of(Period.ZERO, Duration.ofHours(cnt))),
    H4("H4", 240, "H8", "H1", cnt -> TimeframeDuration.of(Period.ZERO, Duration.ofHours(cnt * 4))),
    H8("H8", 480, "D1", "H4", cnt -> TimeframeDuration.of(Period.ZERO, Duration.ofHours(cnt * 8))),
    D1("D1", 1440, "W1", "H8", cnt -> TimeframeDuration.of(Period.ofDays(cnt.intValue()), Duration.ZERO)),
    W1("W1", 10080, "MN1", "D1", cnt -> TimeframeDuration.of(Period.ofWeeks(cnt.intValue()), Duration.ZERO)),
    MN1("MN1", 43200, "MN3", "W1", cnt -> TimeframeDuration.of(Period.ofMonths(cnt.intValue()), Duration.ZERO)),
    MN3("MN3", 129600, "Y1", "MN1", cnt -> TimeframeDuration.of(Period.ofMonths(cnt.intValue() * 3), Duration.ZERO)),
    Y1("Y1", 518400, null, "MN3", cnt -> TimeframeDuration.of(Period.ofYears(cnt.intValue()), Duration.ZERO));

    private final String code;
    private final Integer value;
    private final String nextTimeframe;
    private final String prevTimeframe;
    private final Function<Long, TimeframeDuration> durationSupplier;

    Timeframe(String code,
              Integer value,
              String nextTimeframe,
              String prevTimeframe,
              Function<Long, TimeframeDuration> durationSupplier) {
        this.code = code;
        this.value = value;
        this.nextTimeframe = nextTimeframe;
        this.prevTimeframe = prevTimeframe;
        this.durationSupplier = durationSupplier;
    }

    public String getCode() {
        return code;
    }

    public Integer getValue() {
        return value;
    }

    public Optional<Timeframe> getNext() {
        return nextTimeframe == null
                ? Optional.empty()
                : Optional.of(Timeframe.valueOf(nextTimeframe));
    }

    public Optional<Timeframe> getPrev() {
        return prevTimeframe == null
                ? Optional.empty()
                : Optional.of(Timeframe.valueOf(prevTimeframe));
    }

    public static Optional<Timeframe> findByCode(String code) {
        return Arrays.stream(values()).filter(timeframe -> timeframe.getCode().equals(code))
                .findFirst();
    }

    public boolean isAfter(Timeframe timeframe) {
        return this.value > Objects.requireNonNull(timeframe, "timeframe").getValue();
    }

    public boolean isBefore(Timeframe timeframe) {
        return this.value < Objects.requireNonNull(timeframe, "timeframe").getValue();
    }

    public boolean isNotAfter(Timeframe timeframe) {
        return this.value <= Objects.requireNonNull(timeframe, "timeframe").getValue();
    }

    public boolean isNotBefore(Timeframe timeframe) {
        return this.value >= Objects.requireNonNull(timeframe, "timeframe").getValue();
    }

    public TimeframeDuration getDuration(long count) {
        return durationSupplier.apply(count);
    }

    public TimeframeDuration getDuration() {
        return getDuration(1);
    }

    public static class TimeframeDuration {
        private final Period period;
        private final Duration duration;

        private TimeframeDuration(Period period, Duration duration) {
            this.period = period;
            this.duration = duration;
        }

        public Period getPeriod() {
            return period;
        }

        public Duration getDuration() {
            return duration;
        }

        public long getSeconds() {
            long seconds = 0;
            if (period != null) {
                seconds += (long) period.getDays() * 24 * 60 * 60;
            }
            if (duration != null) {
                seconds += duration.getSeconds();
            }
            return seconds;
        }

        public static TimeframeDuration of(Period period, Duration duration) {
            return new TimeframeDuration(period, duration);
        }
    }
}
