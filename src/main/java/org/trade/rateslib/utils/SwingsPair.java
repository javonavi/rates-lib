package org.trade.rateslib.utils;


import org.trade.rateslib.model.SwingPoint;
import org.trade.rateslib.model.Timeframe;

/**
 * @author Ivanov Andrey
 * @since 19.03.2024
 */
public class SwingsPair {
    private final SwingPoint swing1;
    private final SwingPoint swing2;

    private Double timeDiff;
    private Double priceDiff;

    private SwingsPair(SwingPoint swing1,
                       SwingPoint swing2) {
        this.swing1 = swing1;
        this.swing2 = swing2;
    }

    public static SwingsPair of(SwingPoint swing1, SwingPoint swing2) {
        return new SwingsPair(swing1, swing2);
    }

    public SwingPoint getSwing1() {
        return swing1;
    }

    public SwingPoint getSwing2() {
        return swing2;
    }

    public double getTimeDiff() {
        if (timeDiff == null) {
            timeDiff = Double.valueOf(TimeUtils.calcDuration(Timeframe.findByCode(swing1.getTimeframe()).orElseThrow(),
                    swing1.getTime(), swing2.getTime()));
        }
        return timeDiff;
    }

    public double getPriceDiff() {
        if (priceDiff == null) {
            priceDiff = Math.abs(swing1.getPrice().doubleValue() - swing2.getPrice().doubleValue());
        }
        return priceDiff;
    }

    @Override
    public String toString() {
        return "SwingsPair{" +
                "swing1=" + swing1 +
                ", swing2=" + swing2 +
                '}';
    }
}
