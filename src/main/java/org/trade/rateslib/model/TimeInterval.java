package org.trade.rateslib.model;

import java.time.LocalDateTime;

public class TimeInterval {
    private final LocalDateTime fromTime;
    private final LocalDateTime toTime;
    private final Timeframe timeframe;

    private TimeInterval(LocalDateTime fromTime,
                         LocalDateTime toTime,
                         Timeframe timeframe) {
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.timeframe = timeframe;
    }

    public static TimeInterval of(LocalDateTime fromTime,
                                  LocalDateTime toTime,
                                  Timeframe timeframe) {
        return new TimeInterval(fromTime, toTime, timeframe);
    }
}
