package org.trade.rateslib.data;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author javonavi
 */
public class RateEntity {

    private LocalDateTime time;

    private Double high;

    private Double low;

    private Double close;

    private Double open;

    public RateEntity() {

    }

    public LocalDateTime getTime() {
        return time;
    }

    public Double getHigh() {
        return high;
    }

    public Double getLow() {
        return low;
    }

    public Double getClose() {
        return close;
    }

    public Double getOpen() {
        return open;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    public void setOpen(Double open) {
        this.open = open;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RateEntity rateEntity = (RateEntity) o;
        return getTime().equals(rateEntity.getTime()) && getHigh().equals(rateEntity.getHigh()) && getLow().equals(rateEntity.getLow()) && getClose().equals(rateEntity.getClose()) && getOpen().equals(rateEntity.getOpen());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTime(), getHigh(), getLow(), getClose(), getOpen());
    }

    @Override
    public String toString() {
        return "RateEntity{" +
                "time=" + time +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", open=" + open +
                '}';
    }

}
