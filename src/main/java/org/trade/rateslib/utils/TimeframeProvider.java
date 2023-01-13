package org.trade.rateslib.utils;

import java.util.Optional;

/**
 * @author javonavi
 */
public class TimeframeProvider {

    public static Optional<String> getPrevious(String timeframe) {
        switch (timeframe) {
            case "m5": return Optional.empty();
            case "m15": return Optional.of("m5");
            case "h1": return Optional.of("m15");
            case "h4": return Optional.of("h1");
            case "d1": return Optional.of("h4");
            case "w1": return Optional.of("d1");
            case "mn1": return Optional.of("w1");
            case "y1": return Optional.of("mn1");
            default: return Optional.empty();
        }
    }

}
