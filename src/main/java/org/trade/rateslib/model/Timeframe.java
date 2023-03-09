package org.trade.rateslib.model;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public enum Timeframe {
    M5("M5", 5, "M15", null, Duration.ofMinutes(5)),
    M15("M15", 15, "H1", "M5", Duration.ofMinutes(15)),
    H1("H1", 60, "H4", "M15", Duration.ofHours(1)),
    H4("H4", 240, "D1", "H1", Duration.ofHours(4)),
    D1("D1", 1440, "W1", "H4", Duration.ofDays(1)),
    W1("W1", 10080, "MN1", "D1", Duration.of(1, ChronoUnit.WEEKS)),
    MN1("MN1", 43200, "MN3", "W1", Duration.of(1, ChronoUnit.MONTHS)),
    MN3("MN3", 129600, "Y1", "MN1", Duration.of(3, ChronoUnit.MONTHS)),
    Y1("Y1", 518400, null, "MN3", Duration.of(1, ChronoUnit.YEARS));

    private final String code;
    private final Integer value;
    private final String nextTimeframe;
    private final String prevTimeframe;
    private final Duration duration;

    Timeframe(String code, Integer value, String nextTimeframe, String prevTimeframe, Duration duration) {
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

    public Duration getDuration() {
        return duration;
    }
}
