package org.trade.rateslib.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.lang.Math.ceil;

public enum FiboLevel {

    FIBO_0_062(0.0625),
    FIBO_0_125(0.125),
    FIBO_0_250(0.250),
    FIBO_0_333(0.333),
    FIBO_0_375(0.375),
    FIBO_0_500(0.500),
    FIBO_0_623(0.625),
    FIBO_0_666(0.666),
    FIBO_0_750(0.750),
    FIBO_0_875(0.875),
    FIBO_0_937(0.9375),
    FIBO_1_000(1.000),
    FIBO_1_062(1.0625),
    FIBO_1_125(1.125),
    FIBO_1_250(1.250),
    FIBO_1_333(1.333),
    FIBO_1_375(1.375),
    FIBO_1_500(1.500),
    FIBO_1_623(1.625),
    FIBO_1_666(1.666),
    FIBO_1_750(1.750),
    FIBO_1_875(1.875),
    FIBO_1_937(1.9375),
    FIBO_2_000(2.000);


    private final double value;

    FiboLevel(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public static List<FiboLevel> getAll() {
        return Arrays.stream(values()).sorted(Comparator.comparing(FiboLevel::getValue)).toList();
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

    public static List<FiboLevel> getFull() {
        return Arrays.stream(values())
                .filter(f -> f.getValue() <= 1)
                .sorted(Comparator.comparing(FiboLevel::getValue)).toList();
    }

    public static Set<FiboLevel> getBased() {
        return Set.of(
                FIBO_0_062,
                FIBO_0_125,
                FIBO_0_250,
                FIBO_0_333,
                FIBO_0_500,
                FIBO_0_666,
                FIBO_0_750,
                FIBO_0_875,
                FIBO_0_937,
                FIBO_1_000
        );
    }

    public static Set<Double> getBasedValuesWithZero() {
        return Set.of(
                0.0,
                FIBO_0_062.getValue(),
                FIBO_0_125.getValue(),
                FIBO_0_250.getValue(),
                FIBO_0_333.getValue(),
                FIBO_0_500.getValue(),
                FIBO_0_666.getValue(),
                FIBO_0_750.getValue(),
                FIBO_0_875.getValue(),
                FIBO_0_937.getValue(),
                FIBO_1_000.getValue()
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
                FIBO_2_000
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
