package org.trade.rateslib.indicator;

import org.trade.rateslib.model.Rate;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class BollingerBands {

    public BollingerBandsResult calc(List<Rate> rates,
                                     int fromIndex,
                                     int toIndex) {
        double n = toIndex - fromIndex;
        double center = IntStream.range(fromIndex, toIndex)
                .mapToDouble(i -> rates.get(i).getClose().doubleValue()).sum() / n;
        double sd = sqrt(IntStream.range(fromIndex, toIndex)
                .mapToDouble(i -> pow(rates.get(i).getClose().doubleValue() - center, 2)).sum() / n);
        return new BollingerBandsResult(center + 2 * sd, center, center - 2 * sd);
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
