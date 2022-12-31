package org.trade.rateslib.model;

public enum SwingDirection {
    UP,
    DOWN;

    public static SwingDirection byBoolean(boolean val) {
        return val ? UP : DOWN;
    }

    public boolean toBoolean() {
        return this == UP;
    }
}
