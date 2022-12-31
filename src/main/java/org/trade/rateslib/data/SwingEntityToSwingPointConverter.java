package org.trade.rateslib.data;

import org.trade.rateslib.model.SwingDirection;
import org.trade.rateslib.model.SwingPoint;
import org.trade.rateslib.utils.SwingLengthCalculator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author javonavi
 */
public class SwingEntityToSwingPointConverter {

    public static SwingPoint convert(SwingEntity swingEntity,
                                     String stock,
                                     String timeframe,
                                     LocalDateTime previousSwingTime,
                                     RatesService ratesService) {
        double length = SwingLengthCalculator.calcTimeBetween(timeframe,
                previousSwingTime,
                swingEntity.getTime());
        double lengthInBars = SwingLengthCalculator.calcBarsBetween(ratesService,
                stock,
                timeframe,
                previousSwingTime,
                swingEntity.getTime(),
                timeframe);
                ratesService.getCountBetween(stock, timeframe, previousSwingTime, swingEntity.getTime());
        return SwingPoint.builder()
                .withDirection(SwingDirection.byBoolean(swingEntity.getDirection()))
                .withLength(length)
                .withLengthInBars(lengthInBars)
                .withPrice(BigDecimal.valueOf(swingEntity.getPrice()))
                .withSection(0)
                .withTime(swingEntity.getTime())
                .withTimeframe(timeframe)
                .build();
    }
}
