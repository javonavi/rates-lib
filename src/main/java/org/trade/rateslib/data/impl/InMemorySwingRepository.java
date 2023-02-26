package org.trade.rateslib.data.impl;

import org.trade.rateslib.data.SwingEntity;
import org.trade.rateslib.data.SwingRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * @author javonavi
 */
public class InMemorySwingRepository implements SwingRepository {

    private final TreeMap<LocalDateTime, SwingEntity> tree = new TreeMap<>();

    @Override
    public void deleteByTimeGreaterThan(LocalDateTime time) {
        while (tree.size() > 0 && tree.lastKey().isAfter(time)) {
            tree.remove(tree.pollLastEntry().getKey());
        }
    }

    @Override
    public SwingEntity getByIndex(int index) {
        List<SwingEntity> list = new ArrayList<>(tree.values());
        return list.get(list.size() - index - 1);
    }

    @Override
    public Optional<SwingEntity> findBeforeTime(LocalDateTime time) {
        return Optional.ofNullable(tree.lowerEntry(time)).map(Map.Entry::getValue);
    }

    @Override
    public Optional<SwingEntity> findAfterTime(LocalDateTime time) {
        return Optional.ofNullable(tree.higherEntry(time)).map(Map.Entry::getValue);
    }

    @Override
    public List<SwingEntity> findAllByTimeBetween(LocalDateTime timeStart, LocalDateTime timeEnd) {
        if (timeStart.minusSeconds(timeStart.getSecond()).equals(timeEnd.minusSeconds(timeEnd.getSecond()))) {
            return Collections.singletonList(tree.get(timeStart));
        }
        return new ArrayList<>(tree.subMap(timeStart, true, timeEnd, true).values());
    }

    @Override
    public SwingEntity findByTime(LocalDateTime time) {
        return tree.get(time);
    }

    @Override
    public void save(SwingEntity swingEntity) {
        tree.put(swingEntity.getTime(), swingEntity);
    }

    @Override
    public int count() {
        return tree.size();
    }

    @Override
    public List<SwingEntity> getLatest(int count) {
        List<SwingEntity> list = new ArrayList<>(tree.values());
        if (count > list.size()) {
            throw new RuntimeException("Count more than list size: count=" + count + "; listSize=" + list.size());
        }
        List<SwingEntity> result = list.subList(list.size() - count - 1, list.size() - count);
        Collections.reverse(result);
        return result;
    }
}
