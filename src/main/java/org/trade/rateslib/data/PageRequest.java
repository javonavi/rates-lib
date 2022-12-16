package org.trade.rateslib.data;

/**
 * @author javonavi
 */
public class PageRequest implements Pageable {
    private final int fromIndex;
    private final int toIndex;
    private final Sort sort;

    private PageRequest(int fromIndex, int toIndex, Sort sort) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.sort = sort;
    }

    @Override
    public int getFrom() {
        return fromIndex;
    }

    @Override
    public int getTo() {
        return toIndex;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    public static PageRequest of(int fromIndex, int toIndex) {
        return new PageRequest(fromIndex, toIndex, null);
    }

    public static PageRequest of(int fromIndex, int toIndex, Sort sort) {
        return new PageRequest(fromIndex, toIndex, sort);
    }
}
