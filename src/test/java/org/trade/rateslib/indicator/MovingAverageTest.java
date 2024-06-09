package org.trade.rateslib.indicator;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MovingAverageTest {

    @Test
    public void check() {
        MovingAverage ma = new MovingAverage();

        List<Double> vals = new ArrayList<>();
        vals.add(10.);
        vals.add(20.);
        vals.add(30.);
        vals.add(40.);
        vals.add(50.);
        vals.add(60.);

        List<Double> result = ma.calc(vals, 3, MovingAverage.MovingAverageType.SMA);
        System.out.println(result);
        assertEquals(result.size(), 4);
        assertEquals(result.get(0), 20);
    }
}
