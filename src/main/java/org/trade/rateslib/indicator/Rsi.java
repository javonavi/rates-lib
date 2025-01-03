package org.trade.rateslib.indicator;

import org.trade.rateslib.model.PriceType;
import org.trade.rateslib.model.Rate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Rsi {

    public List<Double> calc(List<Rate> rates,
                             int period,
                             PriceType priceType) {
        List<Double> result = new ArrayList<>();
        RsiCalculator rsiCalculator = new RsiCalculator(period);
        for (int i = 0; i < rates.size(); i++) {
            rsiCalculator.calc(getPrice(rates.get(i), priceType)).ifPresent(result::add);
        }
        return result;
    }

    private double getPrice(Rate rate,
                            PriceType priceType) {
        return switch (priceType) {
            case OPEN -> rate.getOpen().doubleValue();
            case HIGH -> rate.getHigh().doubleValue();
            case LOW -> rate.getLow().doubleValue();
            case CLOSE -> rate.getClose().doubleValue();
        };
    }


    public static class RsiCalculator {
        private final int period;
        private final List<Double> prices;
        private double lastPositive;
        private double lastNegative;

        public RsiCalculator(int period) {
            this.period = period;
            this.prices = new ArrayList<>(period);
            this.lastPositive = 0;
            this.lastNegative = 0;
        }

        public RsiCalculator(int period,
                             List<Double> prices,
                             double lastPositive,
                             double lastNegative) {
            this.period = period;
            this.prices = prices;
            this.lastPositive = lastPositive;
            this.lastNegative = lastNegative;
        }

        public int getPeriod() {
            return period;
        }

        public List<Double> getPrices() {
            return prices;
        }

        public double getLastPositive() {
            return lastPositive;
        }

        public double getLastNegative() {
            return lastNegative;
        }

        public Optional<Double> calc(double price) {
            prices.add(price);
            if (prices.size() < period) {
                return Optional.empty();
            }
            if (prices.size() == period && lastPositive == 0 && lastNegative == 0) {
                double SumP = 0.0;
                double SumN = 0.0;
                double diff;
                for (int i = 1; i < period; i++) {
                    diff = prices.get(i) - prices.get(i - 1);
                    SumP += (diff > 0 ? diff : 0);
                    SumN += (diff < 0 ? -diff : 0);
                }
                lastPositive = SumP / period;
                lastNegative = SumN / period;
                double rsi = 50.0;
                if (lastNegative != 0.0) {
                    rsi = 100.0 - (100.0 / (1.0 + lastPositive / lastNegative));
                } else if (lastPositive != 0.0) {
                    rsi = 100.0;
                }
                return Optional.of(rsi);
            }
            if (prices.size() > period) {
                prices.remove(0);
            }
            double diff = prices.get(prices.size() - 1) - prices.get(prices.size() - 2);
            lastPositive = (lastPositive * (period - 1) + (diff > 0.0 ? diff : 0.0)) / period;
            lastNegative = (lastNegative * (period - 1) + (diff < 0.0 ? -diff : 0.0)) / period;
            double rsi = 50;
            if (lastNegative != 0.0)
                rsi = 100.0 - 100.0 / (1 + lastPositive / lastNegative);
            else if (lastPositive != 0.0) {
                rsi = 100.0;
            }
            return Optional.of(rsi);
        }
    }

}
