package org.trade.rateslib.data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author javonavi
 */
public interface SwingHandlerContextRepository {

    void deleteByTimeGreaterThan(LocalDateTime time);

    List<SwingHandlerContextEntity> findAllByTimeLessThan(LocalDateTime time, Pageable pageable);

    void save(SwingHandlerContextEntity entity);
}
