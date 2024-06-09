package org.trade.rateslib.indicator;

import org.trade.rateslib.model.Rate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BollingerBandsNarrowing {

    private final BollingerBands bollingerBands;
    private final MovingAverage movingAverage;

    public BollingerBandsNarrowing() {
        this.bollingerBands = new BollingerBands();
        this.movingAverage = new MovingAverage();
    }

    public List<Boolean> check(List<Rate> rates,
                               int bollingerPeriod,
                               int maPeriod) {
        List<Double> bollingers = bollingerBands.calc(rates, bollingerPeriod).stream().map((bb) -> {
                    return bb.getUpper() - bb.getLower();
                })
                .map(i -> Math.log(i))
                .collect(Collectors.toList());
        List<Double> ma = movingAverage.calc(bollingers, maPeriod, MovingAverage.MovingAverageType.SMA);
        int startMa = bollingers.size() - ma.size();
        int maIndex = 0;
        List<Boolean> result = new ArrayList<>();
        int resultIndex = 0;
        while (resultIndex < rates.size() - bollingers.size()) {
            result.add(Boolean.FALSE);
            resultIndex++;
        }
        for (int i = 0; i < bollingers.size(); i++) {
            Double maVal = null;
            if (i >= startMa) {
                maVal = ma.get(maIndex);
                maIndex++;
            }
            result.add(maVal != null && bollingers.get(i) < maVal);
            resultIndex++;
        }
        return result;
    }

}
