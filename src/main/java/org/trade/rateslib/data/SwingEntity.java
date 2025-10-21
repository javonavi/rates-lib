package org.trade.rateslib.data;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author javonavi
 */
public class SwingEntity {

    private LocalDateTime time;

    private Boolean direction;

    private Double price;

    private Double lengthInBars;

    private Double length;

    private LocalDateTime generateTime;

    public LocalDateTime getTime() {
        return time;
    }

    public Boolean getDirection() {
        return direction;
    }

    public Double getPrice() {
        return price;
    }

    public Double getLengthInBars() {
        return lengthInBars;
    }

    public Double getLength() {
        return length;
    }

    public LocalDateTime getGenerateTime() {
        return generateTime;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public void setDirection(Boolean direction) {
        this.direction = direction;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setLengthInBars(Double lengthInBars) {
        this.lengthInBars = lengthInBars;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public void setGenerateTime(LocalDateTime generateTime) {
        this.generateTime = generateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SwingEntity that = (SwingEntity) o;
        return getTime().equals(that.getTime()) && getDirection() == that.getDirection() && getPrice().equals(that.getPrice());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTime(), getDirection(), getPrice());
    }

    @Override
    public String toString() {
        return "SwingEntity{" +
                "time=" + time +
                ", direction=" + direction +
                ", price=" + price +
                ", lengthInBars=" + lengthInBars +
                ", length=" + length +
                ", generateTime=" + generateTime +
                '}';
    }
}
