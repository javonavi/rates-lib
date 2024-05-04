package org.trade.rateslib.indicator;

import org.trade.rateslib.model.Rate;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.max;

/**
 * @author Ivanov Andrey
 * @since 04.05.2024
 */
public class Atr {

    public double calc(List<Rate> rates,
                       int fromIndex,
                       int toIndex) {
        double n = toIndex - fromIndex - 1.;
        List<Double> tr = new ArrayList<>();
        for (int i = fromIndex + 1; i < toIndex; i++) {
            tr.add(calcTr(rates, i));
        }
        double trSum = tr.stream().mapToDouble(Double::doubleValue).sum() / n;
        return (trSum * (n - 1) + calcTr(rates, toIndex)) / n;
    }

    public List<Double> calc(List<Rate> rates,
                             int length) {
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < rates.size() - length; i++) {
            result.add(calc(rates, i, i + length - 1));
        }
        return result;
    }

    private double calcTr(List<Rate> rates,
                          int i) {
        Rate cur = rates.get(i);
        double prevDayClose = rates.get(i - 1).getClose().doubleValue();
        return max(
                abs(cur.getHigh().doubleValue() - cur.getLow().doubleValue()),
                max(abs(cur.getHigh().doubleValue() - prevDayClose),
                        abs(cur.getLow().doubleValue() - prevDayClose)));
    }
}
