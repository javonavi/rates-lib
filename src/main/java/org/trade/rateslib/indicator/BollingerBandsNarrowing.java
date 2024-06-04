package org.trade.rateslib.indicator;

import org.trade.rateslib.model.Rate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.min;

public class BollingerBandsNarrowing {

    private final BollingerBands bollingerBands;
    private final MovingAverage movingAverage;

    public BollingerBandsNarrowing() {
        this.bollingerBands = new BollingerBands();
        this.movingAverage = new MovingAverage();
    }

    public List<Boolean> check(List<Rate> rates,
                               int avgBandsWindowCount,
                               int maBandsSmallPeriod,
                               int maBandsLargePeriod) {
        List<Double> bands = bollingerBands.calc(rates, avgBandsWindowCount).stream().map(bb -> bb.getUpper() - bb.getLower()).collect(Collectors.toList());
        if (bands.size() < maBandsLargePeriod) {
            return Collections.emptyList();
        }

        List<Double> bandsMaLarge = movingAverage.calc(bands, maBandsLargePeriod, MovingAverage.MovingAverageType.SMA);
        List<Double> bandsMaSmall = movingAverage.calc(bands, maBandsSmallPeriod, MovingAverage.MovingAverageType.SMA);

        int checkingBars = min(bandsMaLarge.size(), bandsMaSmall.size());

        List<Boolean> result = new ArrayList<>();
        for (int i = 1; i <= checkingBars; i++) {
            result.add(bandsMaLarge.get(bandsMaLarge.size() - i) < bandsMaSmall.get(bandsMaSmall.size() - i));
        }

        Collections.reverse(result);
        return result;
    }

}
