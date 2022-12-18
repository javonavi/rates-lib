package org.trade.rateslib.model;

public enum SwingDirection {
    UP,
    DOWN;

    public static SwingDirection byBoolean(boolean val) {
        return val ? DOWN : UP;
    }

    public boolean toBoolean() {
        return this == UP;
    }
}
