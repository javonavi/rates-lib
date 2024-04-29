package org.trade.rateslib.indicator;

import java.util.List;

public class MovingAverage {

    public double calc(List<Double> values,
                       int length,
                       MovingAverageType type) {
        return values.stream().mapToDouble(Double::doubleValue).sum() / length;
    }

    public static enum MovingAverageType {
        SMA,

        ;
    }

}
