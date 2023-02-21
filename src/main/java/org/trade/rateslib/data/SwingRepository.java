package org.trade.rateslib.data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author javonavi
 */
public interface SwingRepository {

    void deleteByTimeGreaterThan(LocalDateTime time);

    SwingEntity getByIndex(int index);

    Optional<SwingEntity> findBeforeTime(LocalDateTime time);

    Optional<SwingEntity> findAfterTime(LocalDateTime time);

    List<SwingEntity> findAllByTimeBetween(LocalDateTime timeStart, LocalDateTime timeEnd);

    SwingEntity findByTime(LocalDateTime time);

    void save(SwingEntity swingEntity);

    int count();

    List<SwingEntity> getLatest(int count);
}
