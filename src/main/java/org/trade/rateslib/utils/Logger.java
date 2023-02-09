package org.trade.rateslib.utils;

import java.util.logging.Level;

import static java.util.logging.Level.FINEST;

/**
 * @author javonavi
 */
public class Logger {

    private static String toString(Object o) {
        return o == null ? "null" : o.toString();
    }

    private final Level level;

    private Logger(Level level) {
        this.level = level;
    }

    public static Logger getLogger(Level level) {
        return new Logger(level);
    }

    public void log(Level level, String message) {
        if (level.intValue() < this.level.intValue()) {
            return;
        }
        String l = "";
        switch (level.getName()) {
            case "FINEST": l = "TRACE"; break;
            case "FINE": l = "DEBUG"; break;
            case "INFO": l = "INFO"; break;
            case "WARNING": l = "WARN"; break;
            default:
                throw new RuntimeException("Unexpected level: " + level.getName());
        }
        System.out.println("[" + l + "] " + message);
    }

    public void trace(String message) {
        log(FINEST, message);
    }

    public void trace(String message, Object o1) {
        log(FINEST, message.replace("{}", toString(o1)));
    }

    public void debug(String message) {
        log(Level.FINE, message);
    }

    public void debug(String message, Object o1) {
        log(Level.FINE, message.replace("{}", toString(o1)));
    }

    public void debug(String message, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6) {
        log(Level.FINE, message
                .replaceFirst("\\{\\}", toString(o1))
                .replaceFirst("\\{\\}", toString(o2))
                .replaceFirst("\\{\\}", toString(o3))
                .replaceFirst("\\{\\}", toString(o4))
                .replaceFirst("\\{\\}", toString(o5))
                .replaceFirst("\\{\\}", toString(o6)));
    }

    public void info(String message, Object o1, Object o2) {
        log(Level.INFO, message
                .replaceFirst("\\{\\}", toString(o1))
                .replaceFirst("\\{\\}", toString(o2)));
    }

    public void info(String message, Object o1, Object o2, Object o3) {
        log(Level.INFO, message
                .replaceFirst("\\{\\}", toString(o1))
                .replaceFirst("\\{\\}", toString(o2))
                .replaceFirst("\\{\\}", toString(o3)));
    }

    public void info(String message, Object o1, Object o2, Object o3, Object o4) {
        log(Level.INFO, message
                .replaceFirst("\\{\\}", toString(o1))
                .replaceFirst("\\{\\}", toString(o2))
                .replaceFirst("\\{\\}", toString(o3))
                .replaceFirst("\\{\\}", toString(o4)));
    }

    public void info(String message, Object o1, Object o2, Object o3, Object o4, Object o5) {
        log(Level.INFO, message
                .replaceFirst("\\{\\}", toString(o1))
                .replaceFirst("\\{\\}", toString(o2))
                .replaceFirst("\\{\\}", toString(o3))
                .replaceFirst("\\{\\}", toString(o4))
                .replaceFirst("\\{\\}", toString(o5)));
    }

    public void info(String message, Object o1) {
        log(Level.INFO, message
                .replaceFirst("\\{\\}", toString(o1)));
    }

    public void warn(String message, Object o1, Object o2, Object o3) {
        log(Level.WARNING, message
                .replaceFirst("\\{\\}", toString(o1))
                .replaceFirst("\\{\\}", toString(o2))
                .replaceFirst("\\{\\}", toString(o3)));
    }
}
