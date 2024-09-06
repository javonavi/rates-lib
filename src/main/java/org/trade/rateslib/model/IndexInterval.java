package org.trade.rateslib.model;

public class IndexInterval {
    private final int fromIndex;
    private final int toIndex;

    private IndexInterval(int fromIndex, int toIndex) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    public static IndexInterval of(int fromIndex, int toIndex) {
        return new IndexInterval(fromIndex, toIndex);
    }

    public static IndexInterval of(double fromIndex, double toIndex) {
        return new IndexInterval((int) Math.floor(fromIndex), (int) Math.ceil(toIndex));
    }

    public int getFromIndex() {
        return fromIndex;
    }

    public int getToIndex() {
        return toIndex;
    }
}
