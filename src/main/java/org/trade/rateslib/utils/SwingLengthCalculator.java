package org.trade.rateslib.utils;

import org.trade.rateslib.data.RatesService;
import org.trade.rateslib.model.Timeframe;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author javonavi
 */
public class SwingLengthCalculator {

    public static double calcBarsBetween(RatesService ratesService,
                                         String stock,
                                         String timeframe,
                                         LocalDateTime fromTime,
                                         LocalDateTime toTime,
                                         String smallestTimeframe) {
        Timeframe currentTimeframe = Timeframe.valueOf(timeframe.toUpperCase());
        Timeframe endTimeframe = Timeframe.valueOf(smallestTimeframe.toUpperCase());

        if (currentTimeframe == endTimeframe) {
            return ratesService.getCountBetween(stock, endTimeframe.getCode().toLowerCase(), fromTime.plusSeconds(1), toTime);
        }

        // Сначала находим минимальный таймфрейм, на котором можем посчитать среднее количество баров

        LocalDateTime fromTimeInUpperTf = TimeConverter.convertToUpperTimeframe(smallestTimeframe, timeframe, fromTime);
        LocalDateTime toTimeInUpperTf = TimeConverter.convertToUpperTimeframe(smallestTimeframe, timeframe, toTime);

        // Определяем минимальный TF, для которого есть бары на всем интервале
        while (endTimeframe.isBefore(currentTimeframe)) {
            if (ratesService.getLatestRateBeforeTime(stock, endTimeframe.getCode().toLowerCase(), fromTimeInUpperTf).isPresent()) {
                break;
            }
            if (endTimeframe.getNext().isEmpty()) {
                break;
            }
            endTimeframe = endTimeframe.getNext().get();
        }

        if (currentTimeframe == endTimeframe) {
            throw new RuntimeException("Timeframes are identical: " + currentTimeframe + ", " + endTimeframe);
        }

        // Теперь считаем среднее количество баров меньшего периода в рамках рассматриваемого периода

        double smallestTotalCount = ratesService.getCountBetween(stock, endTimeframe.getCode().toLowerCase(), fromTimeInUpperTf.plusSeconds(1), toTimeInUpperTf);
        if (smallestTotalCount <= 0) {
            throw new RuntimeException("Smallest total count wasn't calculated on smallest TF: " + endTimeframe);
        }

        double highestTotalCount = ratesService.getCountBetween(stock, currentTimeframe.getCode().toLowerCase(), fromTimeInUpperTf.plusSeconds(1), toTimeInUpperTf);
        if (highestTotalCount <= 0) {
            throw new RuntimeException("Highest total count wasn't calculated on smallest TF: " + currentTimeframe);
        }

        double result = highestTotalCount;

        double countPerCurrentRate = smallestTotalCount / highestTotalCount;

        // Вычитаем то, что лишнее в начале
        result -= (double) ratesService.getCountBetween(stock, endTimeframe.getCode().toLowerCase(), fromTimeInUpperTf.plusSeconds(1), fromTime) / countPerCurrentRate;

        //Добавляем то, что остутствует в конце
        result += (double) ratesService.getCountBetween(stock, endTimeframe.getCode().toLowerCase(), toTimeInUpperTf.plusSeconds(1), toTime) / countPerCurrentRate;

        return result;
    }

    public static double calcTimeBetween(String timeframe,
                                         LocalDateTime fromTime,
                                         LocalDateTime toTime) {
        //System.out.println("fromTime=" + fromTime + "; toTime=" + toTime);
        Duration duration = Duration.between(fromTime, toTime);
        switch (timeframe.toLowerCase()) {
            case "m5":
                return duration.toMinutes() / 5.;
            case "m15":
                return duration.toMinutes() / 15.;
            case "h1":
                return duration.toMinutes() / 60.;
            case "h4":
                return duration.toMinutes() / 240.;
            case "d1":
                return duration.toMinutes() / 1440.;
            case "w1":
                return duration.toMinutes() / 10080.;
            case "mn1":
                if (fromTime.getYear() == toTime.getYear()
                        && fromTime.getMonthValue() == toTime.getMonthValue()) {
                    // в одном месяце
                    return calcMonthPart(fromTime, toTime);
                } else {
                    // в разных месяцах
                    return calcDurationTimeForMonths(fromTime, toTime);
                }
            default:
                break;
        }
        throw new RuntimeException("Unexpected timeframe: timeframe=" + timeframe);
    }

    private static double calcMonthPart(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        return duration.toMinutes() / (start.toLocalDate().lengthOfMonth() * 1440.);
    }

    private static double calcDurationTimeForMonths(LocalDateTime fromTime, LocalDateTime toTime) {
        int nextMonthYear = fromTime.getYear();
        int nextMonth = fromTime.getMonthValue() + 1;
        if (nextMonth > 12) {
            nextMonth = 1;
            nextMonthYear++;
        }
        LocalDateTime nextMonthDateTime = LocalDateTime.of(nextMonthYear, nextMonth, 1, 0, 0);
        double result = calcMonthPart(fromTime, nextMonthDateTime);
        result += calcMonthPart(LocalDateTime.of(toTime.getYear(), toTime.getMonthValue(), 1, 0, 0), toTime);

        while (nextMonth < toTime.getMonthValue() || nextMonthYear < toTime.getYear()) {
            result += 1.;
            nextMonth++;
            if (nextMonth > 12) {
                nextMonth = 1;
                nextMonthYear++;
            }
        }
        return result;
    }

}
