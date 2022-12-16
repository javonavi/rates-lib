package org.trade.rateslib.model;

/**
 * @author javonavi
 */
public class Swing {
    private final SwingPoint from;
    private final SwingPoint to;

    private Swing(SwingPoint from, SwingPoint to) {
        this.from = from;
        this.to = to;
    }

    public static Builder builder() {
        return new Builder();
    }

    public SwingPoint getFrom() {
        return from;
    }

    public SwingPoint getTo() {
        return to;
    }

    @Override
    public String toString() {
        return "Swing{" +
                "from=" + from +
                ", to=" + to +
                '}';
    }

    public static class Builder {
        private SwingPoint from;
        private SwingPoint to;

        private Builder() {

        }

        public Builder withFrom(SwingPoint from) {
            this.from = from;
            return this;
        }

        public Builder withTo(SwingPoint to) {
            this.to = to;
            return this;
        }

        public Swing build() {
            return new Swing(from, to);
        }
    }
}
