package org.trade.rateslib.data.impl;

import org.trade.rateslib.data.RateEntity;
import org.trade.rateslib.data.RateRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;

/**
 * @author javonavi
 */
public class InMemoryRateRepository implements RateRepository {

    private final TreeMap<LocalDateTime, RateEntity> tree = new TreeMap<>();

    @Override
    public RateEntity findFirstByOrderByTimeDesc() {
        try {
            return tree.lastEntry().getValue();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public int countByTimeGreaterThanEqual(LocalDateTime time) {
        try {
            return findAllByTimeBetween(time, tree.lastKey()).size();
        } catch (NoSuchElementException e) {
            return 0;
        }
    }

    @Override
    public RateEntity getByIndex(int index) {
        List<RateEntity> list = new ArrayList<>(tree.values());
        if (list.isEmpty()) {
            return null;
        }
        return list.get(list.size() - index - 1);
    }

    @Override
    public List<RateEntity> findAllByTimeBetween(LocalDateTime timeStart, LocalDateTime timeEnd) {
        if (timeStart.minusSeconds(timeStart.getSecond()).equals(timeEnd.minusSeconds(timeEnd.getSecond()))) {
            return Collections.singletonList(tree.get(timeStart));
        }
        if (!timeStart.isBefore(timeEnd)) {
            throw new RuntimeException("Start must be greaten than end: timeStart=" + timeStart + "; timeEnd=" + timeEnd);
        }
        return new ArrayList<>(tree.subMap(timeStart, true, timeEnd, true).values());
    }

    @Override
    public int countByTimeBetween(LocalDateTime timeStart, LocalDateTime timeEnd) {
        return findAllByTimeBetween(timeStart, timeEnd).size();
    }

    @Override
    public RateEntity findFirstByTimeLessThanOrderByTimeDesc(LocalDateTime time) {
        try {
            return tree.lowerEntry(time).getValue();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public List<RateEntity> findAllByTimeGreaterThanEqualOrderByTimeAsc(LocalDateTime time) {
        try {
            return findAllByTimeBetween(time, tree.lastKey());
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public void insert(RateEntity rateEntity) {
        tree.put(rateEntity.getTime(), rateEntity);
    }

    @Override
    public void update(RateEntity rateEntity) {
        tree.put(rateEntity.getTime(), rateEntity);
    }

    @Override
    public Optional<RateEntity> findById(LocalDateTime time) {
        return Optional.ofNullable(tree.get(time));
    }

    @Override
    public int count() {
        return tree.size();
    }

    @Override
    public void deleteAll() {
        tree.clear();
    }

    @Override
    public List<RateEntity> getLatest(int count) {
        List<RateEntity> list = new ArrayList<>(tree.values());
        if (count >= list.size()) {
            throw new RuntimeException("Count more than list size: count=" + count + "; listSize=" + list.size());
        }
        List<RateEntity> result = list.subList(list.size() - count - 1, list.size() - 1);
        Collections.reverse(result);
        return result;
    }

    @Override
    public List<RateEntity> getLatest(LocalDateTime beforeTime, int count) {
        List<RateEntity> list = new ArrayList<>(tree.values());
        int i2 = list.size();
        while (--i2 >= 0 && !list.get(i2).getTime().isBefore(beforeTime));
        int i1 = i2 - count + 1;
        if (i1 < 0) {
            i1 = 0;
        }
        List<RateEntity> result = list.subList(i1, i2 + 1);
        Collections.reverse(result);
        return result;
    }
}
