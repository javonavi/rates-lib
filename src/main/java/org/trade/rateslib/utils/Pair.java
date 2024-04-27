package org.trade.rateslib.utils;

/**
 * @author javonavi
 */
public class Pair<T, M> {
    private final T first;
    private final M second;

    public Pair(T first, M second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public M getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
