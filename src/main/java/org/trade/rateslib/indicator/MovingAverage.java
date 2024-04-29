package org.trade.rateslib.indicator;

import java.util.List;

public class MovingAverage {

    public double calc(List<Double> values,
                       int fromIndex,
                       int toIndex,
                       MovingAverageType type) {
        double sum = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            sum += values.get(i);
        }
        return sum / (toIndex - fromIndex + 1);
    }

    public static enum MovingAverageType {
        SMA,

        ;
    }

}
