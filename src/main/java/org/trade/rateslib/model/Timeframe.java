package org.trade.rateslib.model;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public enum Timeframe {
    M5("M5", 5, "M15", null, TimeframeDuration.of(Period.ZERO, Duration.ofMinutes(5))),
    M15("M15", 15, "H1", "M5", TimeframeDuration.of(Period.ZERO, Duration.ofMinutes(15))),
    H1("H1", 60, "H4", "M15", TimeframeDuration.of(Period.ZERO, Duration.ofHours(1))),
    H4("H4", 240, "D1", "H1", TimeframeDuration.of(Period.ZERO, Duration.ofHours(4))),
    D1("D1", 1440, "W1", "H4", TimeframeDuration.of(Period.ofDays(1), Duration.ZERO)),
    W1("W1", 10080, "MN1", "D1", TimeframeDuration.of(Period.ofWeeks(1), Duration.ZERO)),
    MN1("MN1", 43200, "MN3", "W1", TimeframeDuration.of(Period.ofMonths(1), Duration.ZERO)),
    MN3("MN3", 129600, "Y1", "MN1", TimeframeDuration.of(Period.ofMonths(3), Duration.ZERO)),
    Y1("Y1", 518400, null, "MN3", TimeframeDuration.of(Period.ofYears(1), Duration.ZERO));

    private final String code;
    private final Integer value;
    private final String nextTimeframe;
    private final String prevTimeframe;
    private final TimeframeDuration duration;

    Timeframe(String code, Integer value, String nextTimeframe, String prevTimeframe, TimeframeDuration duration) {
        this.code = code;
        this.value = value;
        this.nextTimeframe = nextTimeframe;
        this.prevTimeframe = prevTimeframe;
        this.duration = duration;
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

    public TimeframeDuration getDuration() {
        return duration;
    }

    private static class TimeframeDuration {
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

        public static TimeframeDuration of(Period period, Duration duration) {
            return new TimeframeDuration(period, duration);
        }
    }
}
