package org.trade.rateslib.model;

import java.util.Comparator;
import java.util.LinkedList;

class RatesCollection {
    private final LinkedList<Rate> rates = new LinkedList<>();
    private final int size;

    public RatesCollection(int size) {
        this.size = size;
    }

    public int size() {
        return rates.size();
    }

    public void add(Rate rate) {
        rates.add(rate);
        if (rates.size() > size) {
            rates.removeFirst();
        }
    }

    public Rate lowest() {
        return rates.stream()
                .min(Comparator.comparing(Rate::getLow))
                .orElseThrow();
    }

    public Rate highest() {
        return rates.stream()
                .max(Comparator.comparing(Rate::getHigh))
                .orElseThrow();
    }
}
