package org.trade.rateslib.data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author javonavi
 */
public interface RateRepository {

    RateEntity findFirstByOrderByTimeDesc();

    int countByTimeGreaterThanEqual(LocalDateTime time);

    RateEntity getByIndex(int index);

    List<RateEntity> findAllByTimeBetween(LocalDateTime timeStart, LocalDateTime timeEnd);

    int countByTimeBetween(LocalDateTime timeStart, LocalDateTime timeEnd);

    RateEntity findFirstByTimeLessThanOrderByTimeDesc(LocalDateTime time);

    List<RateEntity> findAllByTimeGreaterThanEqualOrderByTimeAsc(LocalDateTime time);

    void insert(RateEntity rateEntity);

    void update(RateEntity rateEntity);

    Optional<RateEntity> findById(LocalDateTime time);

    int count();

    void deleteAll();

    List<RateEntity> getLatest(int count);

    List<RateEntity> getLatest(LocalDateTime beforeTime,
                               int count);
}
