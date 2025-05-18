package org.trade.rateslib.model;

import org.trade.rateslib.data.RateEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Rate {
    private final LocalDateTime time;
    private final BigDecimal open;
    private final BigDecimal close;
    private final BigDecimal high;
    private final BigDecimal low;
    private final String timeframe;

    private Rate(LocalDateTime time,
                 BigDecimal open,
                 BigDecimal close,
                 BigDecimal high,
                 BigDecimal low,
                 String timeframe) {
        this.time = Objects.requireNonNull(time, "time is null");
        this.open = Objects.requireNonNull(open, "open is null");
        this.close = Objects.requireNonNull(close, "close is null");
        this.high = Objects.requireNonNull(high, "high is null");
        this.low = Objects.requireNonNull(low, "low is null");
        this.timeframe = Objects.requireNonNull(timeframe, "timeframe is null");
    }

    public LocalDateTime getTime() {
        return time;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getClose() {
        return close;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public String getTimeframe() {
        return timeframe;
    }

    @Override
    public String toString() {
        return "Rate{" +
                "time=" + time +
                ", open=" + String.format("%.6f", open) +
                ", high=" + String.format("%.6f", high) +
                ", low=" + String.format("%.6f", low) +
                ", close=" + String.format("%.6f", close) +
                ", timeframe=" + timeframe +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LocalDateTime time;
        private BigDecimal open;
        private BigDecimal close;
        private BigDecimal high;
        private BigDecimal low;
        private String timeframe;

        private Builder() {
        }

        public Builder withTime(LocalDateTime time) {
            this.time = time;
            return this;
        }

        public Builder withOpen(BigDecimal open) {
            this.open = open;
            return this;
        }

        public Builder withOpen(double open) {
            this.open = BigDecimal.valueOf(open);
            return this;
        }

        public Builder withClose(BigDecimal close) {
            this.close = close;
            return this;
        }

        public Builder withClose(double close) {
            this.close = BigDecimal.valueOf(close);
            return this;
        }

        public Builder withHigh(BigDecimal high) {
            this.high = high;
            return this;
        }

        public Builder withHigh(double high) {
            this.high = BigDecimal.valueOf(high);
            return this;
        }

        public Builder withLow(BigDecimal low) {
            this.low = low;
            return this;
        }

        public Builder withLow(double low) {
            this.low = BigDecimal.valueOf(low);
            return this;
        }

        public Builder withTimeframe(String timeframe) {
            this.timeframe = timeframe;
            return this;
        }

        public Builder fromRate(Rate rate) {
            this.low = rate.low;
            this.high = rate.high;
            this.close = rate.close;
            this.open = rate.open;
            this.time = rate.time;
            this.timeframe = rate.timeframe;
            return this;
        }

        public Rate build() {
            return new Rate(time, open, close, high, low, timeframe);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) {
            return false;
        }
        if (!(o instanceof Rate)) {
            return false;
        }
        Rate rate = (Rate) o;
        if (!time.equals(rate.time)) return false;
        return timeframe == rate.timeframe;
    }

    @Override
    public int hashCode() {
        int result = time.hashCode();
        result = 31 * result + timeframe.hashCode();
        return result;
    }

    public RateEntity toEntity() {
        RateEntity r = new RateEntity();
        r.setTime(time);
        r.setOpen(open.doubleValue());
        r.setHigh(high.doubleValue());
        r.setLow(low.doubleValue());
        r.setClose(close.doubleValue());
        return r;
    }
}
