package org.trade.rateslib.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class SwingPoint implements Serializable {
    private final LocalDateTime time;
    private final BigDecimal price;
    private final SwingDirection direction;
    private final Integer section;
    private final String timeframe;
    private final Double length;
    private final Double lengthInBars;

    private SwingPoint(LocalDateTime time,
                       BigDecimal price,
                       SwingDirection direction,
                       Integer section,
                       String timeframe,
                       Double length,
                       Double lengthInBars) {
        this.time = Objects.requireNonNull(time, "time is null");
        this.price = Objects.requireNonNull(price, "price is null");
        this.direction = Objects.requireNonNull(direction, "direction is null");
        this.section = Objects.requireNonNull(section, "section is null");
        this.timeframe = Objects.requireNonNull(timeframe, "timeframe is null");
        this.length = length;
        this.lengthInBars = lengthInBars;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public SwingDirection getDirection() {
        return direction;
    }

    public Integer getSection() {
        return section;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public Double getLength() {
        if (Objects.isNull(length)) {
            throw new RuntimeException("Swing length wasn't initialized: swing=" + toString());
        }
        return length;
    }

    public Optional<Double> getLengthOpt() {
        return Optional.ofNullable(length);
    }

    public Double getLengthInBars() {
        if (Objects.isNull(lengthInBars)) {
            throw new RuntimeException("Swing lengthInBars wasn't initialized: swing=" + toString());
        }
        return lengthInBars;
    }

    public Optional<Double> getlengthInBarsOpt() {
        return Optional.ofNullable(lengthInBars);
    }

    @Override
    public String toString() {
        return "SwingPoint{" +
                "time=" + time +
                ", price=" + String.format("%.6f", price) +
                ", direction=" + direction +
                ", section=" + section +
                ", timeframe=" + timeframe +
                ", length=" + length +
                ", lengthInBars=" + lengthInBars +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SwingPoint that = (SwingPoint) o;

        if (!time.equals(that.time)) return false;
        if (!price.equals(that.price)) return false;
        return direction == that.direction;
    }

    @Override
    public int hashCode() {
        int result = time.hashCode();
        result = 31 * result + price.hashCode();
        result = 31 * result + direction.hashCode();
        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LocalDateTime time;
        private BigDecimal price;
        private SwingDirection direction;
        private Integer section;
        private String timeframe;
        private Double length;
        private Double lengthInBars;

        public Builder fromSwingPoint(SwingPoint swingPoint) {
            this.time = swingPoint.getTime();
            this.price = swingPoint.getPrice();
            this.direction = swingPoint.getDirection();
            this.section = swingPoint.getSection();
            this.timeframe = swingPoint.getTimeframe();
            this.length = swingPoint.getLengthOpt().orElse(null);
            this.lengthInBars = swingPoint.getlengthInBarsOpt().orElse(null);
            return this;
        }

        public Builder withTime(LocalDateTime time) {
            this.time = time;
            return this;
        }

        public Builder withPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder withDirection(SwingDirection direction) {
            this.direction = direction;
            return this;
        }

        public Builder withSection(Integer section) {
            this.section = section;
            return this;
        }

        public Builder withTimeframe(String timeframe) {
            this.timeframe = timeframe;
            return this;
        }

        public Builder withLength(Double length) {
            this.length = length;
            return this;
        }

        public Builder withLengthInBars(Double lengthInBars) {
            this.lengthInBars = lengthInBars;
            return this;
        }

        public SwingPoint build() {
            return new SwingPoint(
                    time,
                    price,
                    direction,
                    section,
                    timeframe,
                    length,
                    lengthInBars
            );
        }
    }
}
