package org.trade.rateslib.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.lang.Math.ceil;

public enum FiboLevel {

    FIBO_0_125(0.125),
    FIBO_0_250(0.250),
    FIBO_0_333(0.333),
    FIBO_0_500(0.500),
    FIBO_0_666(0.666),
    FIBO_0_750(0.750),
    FIBO_0_875(0.875),
    FIBO_1_000(1.000),
    FIBO_1_125(1.125),
    FIBO_1_250(1.250),
    FIBO_1_333(1.333),
    FIBO_1_500(1.500),
    FIBO_1_666(1.666),
    FIBO_1_750(1.750),
    FIBO_1_875(1.875),
    FIBO_2_000(2.000),
    FIBO_2_125(2.125),
    FIBO_2_250(2.250),
    FIBO_2_333(2.333),
    FIBO_2_500(2.500),
    FIBO_2_666(2.666),
    FIBO_2_750(2.750),
    FIBO_2_875(2.875),
    FIBO_3_000(3.000),
    FIBO_3_125(3.125),
    FIBO_3_250(3.250),
    FIBO_3_333(3.333),
    FIBO_3_500(3.500),
    FIBO_3_666(3.666),
    FIBO_3_750(3.750),
    FIBO_3_875(3.875),
    FIBO_4_000(4.000),
    FIBO_4_125(4.125),
    FIBO_4_250(4.250),
    FIBO_4_333(4.333),
    FIBO_4_500(4.500),
    FIBO_4_666(4.666),
    FIBO_4_750(4.750),
    FIBO_4_875(4.875),
    FIBO_5_000(5.000),
    FIBO_5_125(5.125),
    FIBO_5_250(5.250),
    FIBO_5_333(5.333),
    FIBO_5_500(5.500),
    FIBO_5_666(5.666),
    FIBO_5_750(5.750),
    FIBO_5_875(5.875),
    FIBO_6_000(6.000),
    FIBO_6_125(6.125),
    FIBO_6_250(6.250),
    FIBO_6_333(6.333),
    FIBO_6_500(6.500),
    FIBO_6_666(6.666),
    FIBO_6_750(6.750),
    FIBO_6_875(6.875),
    FIBO_7_000(7.000),
    FIBO_7_125(7.125),
    FIBO_7_250(7.250),
    FIBO_7_333(7.333),
    FIBO_7_500(7.500),
    FIBO_7_666(7.666),
    FIBO_7_750(7.750),
    FIBO_7_875(7.875),
    FIBO_8_000(8.000),
    FIBO_8_125(8.125),
    FIBO_8_250(8.250),
    FIBO_8_333(8.333),
    FIBO_8_500(8.500),
    FIBO_8_666(8.666),
    FIBO_8_750(8.750),
    FIBO_8_875(8.875),
    FIBO_9_000(9.000),
    FIBO_9_125(9.125),
    FIBO_9_250(9.250),
    FIBO_9_333(9.333),
    FIBO_9_500(9.500),
    FIBO_9_666(9.666),
    FIBO_9_750(9.750),
    FIBO_9_875(9.875),
    FIBO_10_000(10.000),
    FIBO_10_125(10.125),
    FIBO_10_250(10.250),
    FIBO_10_333(10.333),
    FIBO_10_500(10.500),
    FIBO_10_666(10.666),
    FIBO_10_750(10.750),
    FIBO_10_875(10.875),
    FIBO_11_000(11.000),
    FIBO_11_125(11.125),
    FIBO_11_250(11.250),
    FIBO_11_333(11.333),
    FIBO_11_500(11.500),
    FIBO_11_666(11.666),
    FIBO_11_750(11.750),
    FIBO_11_875(11.875),
    FIBO_12_000(12.000),
    FIBO_12_125(12.125),
    FIBO_12_250(12.250),
    FIBO_12_333(12.333),
    FIBO_12_500(12.500),
    FIBO_12_666(12.666),
    FIBO_12_750(12.750),
    FIBO_12_875(12.875),
    FIBO_13_000(13.000),
    FIBO_13_125(13.125),
    FIBO_13_250(13.250),
    FIBO_13_333(13.333),
    FIBO_13_500(13.500),
    FIBO_13_666(13.666),
    FIBO_13_750(13.750),
    FIBO_13_875(13.875),
    FIBO_14_000(14.000),
    FIBO_14_125(14.125),
    FIBO_14_250(14.250),
    FIBO_14_333(14.333),
    FIBO_14_500(14.500),
    FIBO_14_666(14.666),
    FIBO_14_750(14.750),
    FIBO_14_875(14.875),
    FIBO_15_000(15.000);


    private final double value;

    FiboLevel(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public static Set<FiboLevel> getCommonBased() {
        return Set.of(
                FIBO_0_250,
                FIBO_0_333,
                FIBO_0_500,
                FIBO_0_666,
                FIBO_0_750,
                FIBO_1_000
        );
    }

    public static Set<FiboLevel> getBased() {
        return Set.of(
                FIBO_0_125,
                FIBO_0_250,
                FIBO_0_333,
                FIBO_0_500,
                FIBO_0_666,
                FIBO_0_750,
                FIBO_0_875,
                FIBO_1_000
        );
    }

    public static Set<FiboLevel> getCommon() {
        return Set.of(
                FIBO_0_250,
                FIBO_0_333,
                FIBO_0_500,
                FIBO_0_666,
                FIBO_0_750,
                FIBO_1_000,
                FIBO_1_250,
                FIBO_1_333,
                FIBO_1_500,
                FIBO_1_666,
                FIBO_1_750,
                FIBO_2_000,
                FIBO_2_250,
                FIBO_2_333,
                FIBO_2_500,
                FIBO_2_666,
                FIBO_2_750,
                FIBO_3_000,
                FIBO_3_250,
                FIBO_3_333,
                FIBO_3_500,
                FIBO_3_666,
                FIBO_3_750,
                FIBO_4_000,
                FIBO_4_250,
                FIBO_4_333,
                FIBO_4_500,
                FIBO_4_666,
                FIBO_4_750,
                FIBO_5_000
        );
    }
    /* maybe wrong realization
        public static Optional<FiboLevel> checkNumeric(double numeric, double maxError) {
            double maxDoubleError = maxError * 2;
            for (FiboLevel fiboLevel : values()) {
                if (fiboLevel.getValue() > numeric + maxDoubleError) {
                    return Optional.empty();
                }
                if (fiboLevel.getValue() > numeric - maxError && fiboLevel.getValue() < numeric + maxError) {
                    return Optional.of(fiboLevel);
                }
            }
            return Optional.empty();
        }
    */
    public static Optional<FiboLevel> checkNumeric(double numeric, double maxError) {
        return checkNumeric(numeric, maxError, List.of(values()));
    }

    public static Optional<FiboLevel> checkNumeric(double numeric, double maxError, Collection<FiboLevel> values) {
        for (FiboLevel fiboLevel : values) {
            if (checkByFibo(fiboLevel, numeric, maxError)) {
                return Optional.of(fiboLevel);
            }
        }
        return Optional.empty();
    }

    private static boolean checkByFibo(FiboLevel fiboLevel, double numeric, double maxError) {
        double err = abs(numeric - fiboLevel.getValue());
        /*
        if (err >= 1) {
            err -= 1;
        } else {
            err = 1 - err;
        }
        if (err < 0) {
            return false;
        }
         */
        return (err <= maxError);
    }

    public static List<Double> getUntilValue(double maxValue) {
        List<Double> based = getBased().stream().map(FiboLevel::getValue).collect(Collectors.toList());
        List<Double> result = new ArrayList<>();
        int iteration = (int) ceil(maxValue);
        for (int i = 0; i < iteration; i++) {
            final int z = i;
            result.addAll(based.stream().map(d -> d + z).filter(d -> d <= maxValue).collect(Collectors.toList()));
        }
        return result.stream().sorted(Comparator.comparing(Double::doubleValue)).collect(Collectors.toList());
    }
}
