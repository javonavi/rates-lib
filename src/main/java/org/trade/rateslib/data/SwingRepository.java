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

    List<SwingEntity> findBeforeTime(LocalDateTime time, int count);

    Optional<SwingEntity> findAfterTime(LocalDateTime time);

    List<SwingEntity> findAfterTime(LocalDateTime time, int count);

    List<SwingEntity> findAllByTimeBetween(LocalDateTime timeStart, LocalDateTime timeEnd);

    SwingEntity findByTime(LocalDateTime time);

    void save(SwingEntity swingEntity);

    int count();

    List<SwingEntity> getLatest(int count);

    Optional<Integer> getShift(LocalDateTime time);

    SwingEntity getLowest();

    SwingEntity getHighest();

    /**
     * Ищет соседние свинги того же направления
     *
     * @param swing Свинг, для которого ищем соседей
     * @param steps Сколько шагов на поиск
     * @return Соседние свинги, отсортированные по времени
     */
    List<SwingEntity> findNearbySwings(SwingEntity swing,
                                       int steps);

    List<SwingEntity> getAll();
}
