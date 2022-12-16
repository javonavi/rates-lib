package org.trade.rateslib.data;

/**
 * @author javonavi
 */
public class Sort {
    private final String field;
    private boolean ascending = true;

    private Sort(String field) {
        this.field = field;
    }

    public Sort descending() {
        ascending = false;
        return this;
    }

    public static Sort by(String field) {
        return new Sort(field);
    }
}
