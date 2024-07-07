package org.trade.rateslib.model;

public class StockTimeframe {

    private final String stock;
    private final Timeframe timeframe;

    private StockTimeframe(String stock, Timeframe timeframe) {
        this.stock = stock;
        this.timeframe = timeframe;
    }

    public static StockTimeframe of(String stock, Timeframe timeframe) {
        return new StockTimeframe(stock, timeframe);
    }

    public String getStock() {
        return stock;
    }

    public Timeframe getTimeframe() {
        return timeframe;
    }

    public String getTimeframeCode() {
        return timeframe.getCode();
    }

    @Override
    public String toString() {
        return "StockTimeframe{" +
                "stock='" + stock + '\'' +
                ", timeframe=" + timeframe +
                '}';
    }
}
