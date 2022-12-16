package org.trade.rateslib.data;

/**
 * @author javonavi
 */
public interface Pageable {

    int getFrom();

    int getTo();

    Sort getSort();
}
