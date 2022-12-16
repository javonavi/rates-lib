package org.trade.rateslib.utils;

import org.trade.rateslib.model.Timeframe;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class TimeConverter {

    public static LocalDateTime convertToUpperTimeframe(String fromTimeframe,
                                                        String toTimeframe,
                                                        LocalDateTime time) {
        return convertToUpperTimeframe(
                Timeframe.valueOf(fromTimeframe.toUpperCase()),
                Timeframe.valueOf(toTimeframe.toUpperCase()),
                time);
    }

    public static LocalDateTime convertToUpperTimeframe(Timeframe fromTimeframe,
                                                        Timeframe toTimeframe,
                                                        LocalDateTime time) {
        if (fromTimeframe.getValue() > toTimeframe.getValue()) {
            return null;
        }
        if (fromTimeframe.getValue() == toTimeframe.getValue()) {
            return time;
        }
        //System.out.println("toTimeframe=" + toTimeframe);
        Timeframe curTimeframe = fromTimeframe;
        //System.out.println("curTimeframe=" + curTimeframe);
        LocalDateTime curTime = time;
        //System.out.println("curTime=" + curTime);
        while (curTimeframe.getValue() < toTimeframe.getValue()) {
            curTime = convertToNextTimeframe(curTimeframe, curTime);
            if (curTime == null) {
                return null;
            }
            if (curTimeframe.getNext().isEmpty()) {
                break;
            }
            curTimeframe = curTimeframe.getNext().get();
        }
        return curTime;
    }

    public static LocalDateTime convertToNextTimeframe(Timeframe currentTimeframe,
                                                       LocalDateTime time) {
        Objects.requireNonNull(currentTimeframe, "currentTimeframe");
        Objects.requireNonNull(time, "time");
        if (!currentTimeframe.getNext().isPresent()) {
            return null;
        }
        int month;
        switch (currentTimeframe.getNext().get()) {
            case M5:
                return calcTimeForMinutes(time, 5);
            case M15:
                return calcTimeForMinutes(time, 15);
            case H1:
                return calcTimeForHours(time, 1);
            case H4:
                return calcTimeForHours(time, 4);
            case D1:
                LocalDateTime calcedTimeD1 = time.minusMinutes(time.getMinute());
                return calcedTimeD1.minusHours(calcedTimeD1.getHour());
            case W1:
                LocalDateTime calcedTimeW1 = time.minusMinutes(time.getMinute());
                calcedTimeW1 = calcedTimeW1.minusHours(calcedTimeW1.getHour());
                if (calcedTimeW1.getDayOfWeek().getValue() == 7) {
                    return calcedTimeW1;
                }
                return calcedTimeW1.minusDays(calcedTimeW1.getDayOfWeek().getValue());
            case MN1:
                LocalDateTime calcedTimeMN1 = time.minusMinutes(time.getMinute());
                calcedTimeMN1 = calcedTimeMN1.minusHours(calcedTimeMN1.getHour());
                return calcedTimeMN1.minusDays(calcedTimeMN1.getDayOfMonth() - 1);
            case MN3:
                month = time.getMonth().getValue();
                if (month >= 3) month -= (month % 3);
                return LocalDateTime.of(time.getYear(), month, 1, 0, 0);
            case Y1:
                return LocalDateTime.of(time.getYear(), 1, 1, 0, 0);
            default:
                break;
        }
        return null;
        //throw new RuntimeException("Wrong timeframe " + currentTimeframe);
    }

    private static LocalDateTime calcTimeForMinutes(LocalDateTime time, int koef) {
        if (time.getMinute() % koef != 0) {
            return time.minusMinutes(time.getMinute() % koef);
        }
        return time;
    }

    private static LocalDateTime calcTimeForHours(LocalDateTime time, int koef) {
        LocalDateTime calcedTime = time.minusMinutes(time.getMinute());
        if (calcedTime.getHour() % koef != 0) {
            return calcedTime.minusHours(calcedTime.getHour() % koef);
        }
        return calcedTime;
    }
}
