package org.trade.rateslib.indicator;

import java.util.ArrayList;
import java.util.List;

public class MovingAverage {

    public double calc(List<Double> values,
                       int fromIndex,
                       int toIndex,
                       MovingAverageType type) {
        double sum = 0;
        for (int i = fromIndex; i <= toIndex; i++) {
            sum += values.get(i);
        }
        return sum / (toIndex - fromIndex + 1);
    }

    public List<Double> calc(List<Double> values,
                             int length,
                             MovingAverageType type) {
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < values.size() - length + 1; i++) {
            result.add(calc(values, i, i + length - 1, type));
        }
        return result;
    }

    public enum MovingAverageType {
        SMA,

        ;
    }

}
