package org.trade.rateslib.indicator;

import org.trade.rateslib.model.Rate;

import java.util.ArrayList;
import java.util.List;

public class BollingerBands {

    public BollingerBandsResult calc(List<Rate> rates,
                                     int fromIndex,
                                     int toIndex) {
        double n = (double)(toIndex - fromIndex) + 1.0;
        double center = 0.;
        for (int i = fromIndex; i <= toIndex; i++) {
            center += rates.get(i).getClose().doubleValue();
        }
        center /= n;
        double sd = 0.;
        for (int i = fromIndex; i <= toIndex; i++) {
            sd += Math.pow(rates.get(i).getClose().doubleValue() - center, 2.0);
        }
        sd /= n;
        sd = Math.sqrt(sd);
        return new BollingerBands.BollingerBandsResult(center + 2.0 * sd, center, center - 2.0 * sd);
    }

    public List<BollingerBandsResult> calc(List<Rate> rates,
                                           int length) {
        List<BollingerBandsResult> result = new ArrayList<>();
        for (int i = 0; i < rates.size() - length; i++) {
            result.add(calc(rates, i, i + length - 1));
        }
        return result;
    }

    public static class BollingerBandsResult {
        private final double upper;
        private final double center;
        private final double lower;

        public BollingerBandsResult(double upper, double center, double lower) {
            this.upper = upper;
            this.center = center;
            this.lower = lower;
        }

        public double getUpper() {
            return upper;
        }

        public double getCenter() {
            return center;
        }

        public double getLower() {
            return lower;
        }
    }
}
