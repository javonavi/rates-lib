package org.trade.rateslib.data.impl;

import org.trade.rateslib.data.RateEntity;
import org.trade.rateslib.data.RateRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий котировок, хранящийся в файлах
 *
 * @author Ivanov Andrey
 * @since 23.02.2024
 */
public class FileStorageRateRepository implements RateRepository {

    @Override
    public RateEntity findFirstByOrderByTimeDesc() {
        return null;
    }

    @Override
    public int countByTimeGreaterThanEqual(LocalDateTime time) {
        return 0;
    }

    @Override
    public RateEntity getByIndex(int index) {
        return null;
    }

    @Override
    public List<RateEntity> findAllByTimeBetween(LocalDateTime timeStart, LocalDateTime timeEnd) {
        return null;
    }

    @Override
    public int countByTimeBetween(LocalDateTime timeStart, LocalDateTime timeEnd) {
        return 0;
    }

    @Override
    public RateEntity findFirstByTimeLessThanOrderByTimeDesc(LocalDateTime time) {
        return null;
    }

    @Override
    public List<RateEntity> findAllByTimeGreaterThanEqualOrderByTimeAsc(LocalDateTime time) {
        return null;
    }

    @Override
    public void insert(RateEntity rateEntity) {

    }

    @Override
    public void update(RateEntity rateEntity) {

    }

    @Override
    public Optional<RateEntity> findById(LocalDateTime time) {
        return Optional.empty();
    }

    @Override
    public int count() {
        return 0;
    }

    @Override
    public void deleteAll() {

    }

    @Override
    public List<RateEntity> getLatest(int count) {
        return null;
    }
}
