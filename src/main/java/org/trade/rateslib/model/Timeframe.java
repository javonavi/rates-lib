package org.trade.rateslib.model;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public enum Timeframe {
    M5("M5", 5, "M15", null),
    M15("M15", 15, "H1", "M5"),
    H1("H1", 60, "H4", "M15"),
    H4("H4", 240, "D1", "H1"),
    D1("D1", 1440, "W1", "H4"),
    W1("W1", 10080, "MN1", "D1"),
    MN1("MN1", 43200, "MN3", "W1"),
    MN3("MN3", 129600, "Y1", "MN1"),
    Y1("Y1", 518400, null, "MN3");

    private final String code;
    private final Integer value;
    private final String nextTimeframe;
    private final String prevTimeframe;

    Timeframe(String code, Integer value, String nextTimeframe, String prevTimeframe) {
        this.code = code;
        this.value = value;
        this.nextTimeframe = nextTimeframe;
        this.prevTimeframe = prevTimeframe;
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
}
