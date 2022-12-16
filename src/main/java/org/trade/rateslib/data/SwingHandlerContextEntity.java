package org.trade.rateslib.data;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author javonavi
 */
public class SwingHandlerContextEntity {

    private LocalDateTime time;

    private Double localHigh;

    private Double localLow;

    private Double currentHigh;

    private Double currentLow;

    private Double globalLow;

    private Double globalHigh;

    private Boolean currentDirection;

    private Integer reverseBarsCount;

    private Integer waitingReverseCount;

    private LocalDateTime lastWorkingPoint;

    private Integer barsUpCount = 0;

    private Integer barsDownCount = 0;

    private Double lastUpSwing;

    private Double lastDownSwing;

    private Double lastWorkingPrice;


    public LocalDateTime getTime() {
        return time;
    }

    public Double getLocalHigh() {
        return localHigh;
    }

    public Double getLocalLow() {
        return localLow;
    }

    public Double getCurrentHigh() {
        return currentHigh;
    }

    public Double getCurrentLow() {
        return currentLow;
    }

    public Double getGlobalLow() {
        return globalLow;
    }

    public Double getGlobalHigh() {
        return globalHigh;
    }

    public Boolean getCurrentDirection() {
        return currentDirection;
    }

    public Integer getReverseBarsCount() {
        return reverseBarsCount;
    }

    public Integer getWaitingReverseCount() {
        return waitingReverseCount;
    }

    public LocalDateTime getLastWorkingPoint() {
        return lastWorkingPoint;
    }

    public Integer getBarsUpCount() {
        return barsUpCount;
    }

    public Integer getBarsDownCount() {
        return barsDownCount;
    }

    public Double getLastUpSwing() {
        return lastUpSwing;
    }

    public Double getLastDownSwing() {
        return lastDownSwing;
    }

    public Double getLastWorkingPrice() {
        return lastWorkingPrice;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public void setLocalHigh(Double localHigh) {
        this.localHigh = localHigh;
    }

    public void setLocalLow(Double localLow) {
        this.localLow = localLow;
    }

    public void setCurrentHigh(Double currentHigh) {
        this.currentHigh = currentHigh;
    }

    public void setCurrentLow(Double currentLow) {
        this.currentLow = currentLow;
    }

    public void setGlobalLow(Double globalLow) {
        this.globalLow = globalLow;
    }

    public void setGlobalHigh(Double globalHigh) {
        this.globalHigh = globalHigh;
    }

    public void setCurrentDirection(Boolean currentDirection) {
        this.currentDirection = currentDirection;
    }

    public void setReverseBarsCount(Integer reverseBarsCount) {
        this.reverseBarsCount = reverseBarsCount;
    }

    public void setWaitingReverseCount(Integer waitingReverseCount) {
        this.waitingReverseCount = waitingReverseCount;
    }

    public void setLastWorkingPoint(LocalDateTime lastWorkingPoint) {
        this.lastWorkingPoint = lastWorkingPoint;
    }

    public void setBarsUpCount(Integer barsUpCount) {
        this.barsUpCount = barsUpCount;
    }

    public void setBarsDownCount(Integer barsDownCount) {
        this.barsDownCount = barsDownCount;
    }

    public void setLastUpSwing(Double lastUpSwing) {
        this.lastUpSwing = lastUpSwing;
    }

    public void setLastDownSwing(Double lastDownSwing) {
        this.lastDownSwing = lastDownSwing;
    }

    public void setLastWorkingPrice(Double lastWorkingPrice) {
        this.lastWorkingPrice = lastWorkingPrice;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SwingHandlerContextEntity that = (SwingHandlerContextEntity) o;
        return getTime().equals(that.getTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTime());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "time=" + time +
                ", localHigh=" + localHigh +
                ", localLow=" + localLow +
                ", currentHigh=" + currentHigh +
                ", currentLow=" + currentLow +
                ", globalLow=" + globalLow +
                ", globalHigh=" + globalHigh +
                ", currentDirection=" + currentDirection +
                ", reverseBarsCount=" + reverseBarsCount +
                ", waitingReverseCount=" + waitingReverseCount +
                ", lastWorkingPoint=" + lastWorkingPoint +
                ", barsUpCount=" + barsUpCount +
                ", barsDownCount=" + barsDownCount +
                ", lastUpSwing=" + lastUpSwing +
                ", lastDownSwing=" + lastDownSwing +
                ", lastWorkingPrice=" + lastWorkingPrice +
                '}';
    }

    public void incWaitingReverseCount() {
        Integer count = getWaitingReverseCount();
        if (count == null) {
            setWaitingReverseCount(1);
        } else {
            setWaitingReverseCount(count + 1);
        }
    }
}
