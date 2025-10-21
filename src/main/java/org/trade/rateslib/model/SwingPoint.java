package org.trade.rateslib.model;

import org.trade.rateslib.data.SwingEntity;

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
    private final LocalDateTime generateTime;

    private SwingPoint(LocalDateTime time,
                       BigDecimal price,
                       SwingDirection direction,
                       Integer section,
                       String timeframe,
                       Double length,
                       Double lengthInBars,
                       LocalDateTime generateTime) {
        this.time = Objects.requireNonNull(time, "time is null");
        this.price = Objects.requireNonNull(price, "price is null");
        this.direction = Objects.requireNonNull(direction, "direction is null");
        this.section = Objects.requireNonNull(section, "section is null");
        this.timeframe = Objects.requireNonNull(timeframe, "timeframe is null");
        this.length = length;
        this.lengthInBars = lengthInBars;
        this.generateTime = generateTime;
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
        return length;
    }

    public Optional<Double> getLengthOpt() {
        return Optional.ofNullable(length);
    }

    public Double getLengthInBars() {
        return lengthInBars;
    }

    public Optional<Double> getlengthInBarsOpt() {
        return Optional.ofNullable(lengthInBars);
    }

    public Optional<Timeframe> getTf() {
        return Timeframe.findByCode(timeframe);
    }

    public LocalDateTime getGenerateTime() {
        return generateTime;
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
                ", generateTime=" + generateTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SwingPoint that = (SwingPoint) o;

        if (!time.equals(that.time)) return false;
        if (!price.equals(that.price)) return false;
        if (!timeframe.equals(that.timeframe)) return false;
        return direction == that.direction;
    }

    @Override
    public int hashCode() {
        int result = time.hashCode();
        result = 31 * result + price.hashCode();
        result = 31 * result + direction.hashCode();
        result = 31 * result + timeframe.hashCode();
        return result;
    }

    public SwingEntity getEntity() {
        SwingEntity swing = new SwingEntity();
        swing.setDirection(getDirection().toBoolean());
        swing.setTime(getTime());
        swing.setPrice(getPrice().doubleValue());
        return swing;
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
        private LocalDateTime generateTime;

        public Builder fromSwingPoint(SwingPoint swingPoint) {
            this.time = swingPoint.getTime();
            this.price = swingPoint.getPrice();
            this.direction = swingPoint.getDirection();
            this.section = swingPoint.getSection();
            this.timeframe = swingPoint.getTimeframe();
            this.length = swingPoint.getLengthOpt().orElse(null);
            this.lengthInBars = swingPoint.getlengthInBarsOpt().orElse(null);
            this.generateTime = swingPoint.getGenerateTime();
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

        public Builder withGenerateTime(LocalDateTime generateTime) {
            this.generateTime = generateTime;
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
                    lengthInBars,
                    generateTime
            );
        }
    }
}
