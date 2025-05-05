package org.trade.rateslib.data.impl;

import org.trade.rateslib.data.SwingEntity;
import org.trade.rateslib.data.SwingRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author javonavi
 */
public class InMemorySwingRepository implements SwingRepository {

    private final TreeMap<LocalDateTime, SwingEntity> tree = new TreeMap<>();
    private final Map<LocalDateTime, Integer> numberMap = new HashMap<>();
    private final List<LocalDateTime> keysList = new ArrayList<>();

    @Override
    public void deleteByTimeGreaterThan(LocalDateTime time) {
        while (tree.size() > 0 && tree.lastKey().isAfter(time)) {
            LocalDateTime key = tree.pollLastEntry().getKey();
            tree.remove(key);
            numberMap.remove(key);
        }
        while (keysList.get(keysList.size()-1).isAfter(time)) {
            keysList.remove(keysList.size()-1);
        }
    }

    @Override
    public SwingEntity getByIndex(int index) {
        if (index >= keysList.size()) {
            return null;
        }
        int ind = keysList.size() - index - 1;
        return tree.get(keysList.get(ind));
    }

    @Override
    public Optional<SwingEntity> findBeforeTime(LocalDateTime time) {
        return Optional.ofNullable(tree.lowerEntry(time)).map(Map.Entry::getValue);
    }

    @Override
    public List<SwingEntity> findBeforeTime(LocalDateTime time, int count) {
        LocalDateTime curTime = time;
        List<SwingEntity> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            var v = Optional.ofNullable(tree.lowerEntry(curTime)).map(Map.Entry::getValue);
            if (v.isEmpty()) {
                break;
            }
            curTime = v.orElseThrow().getTime();
            result.add(v.orElseThrow());
        }
        return result.stream().sorted(Comparator.comparing(SwingEntity::getTime)).toList();
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
        numberMap.put(swingEntity.getTime(), numberMap.size());
        keysList.add(swingEntity.getTime());
    }

    @Override
    public int count() {
        return tree.size();
    }

    @Override
    public List<SwingEntity> getLatest(int count) {
        if (tree.isEmpty()) {
            return Collections.emptyList();
        }
        int cnt = count;
        if (tree.size() < cnt) {
            cnt = tree.size();
        }
        Map.Entry<LocalDateTime, SwingEntity> entry = tree.lastEntry();
        List<SwingEntity> result = new ArrayList<>();
        result.add(entry.getValue());
        for (int i = 1; i < cnt; i++) {
            entry = tree.lowerEntry(entry.getKey());
            result.add(entry.getValue());
        }
        return result;
    }

    @Override
    public Optional<Integer> getShift(LocalDateTime time) {
        Integer number = numberMap.get(time);
        if (number == null) {
            return Optional.empty();
        }
        return Optional.of(numberMap.size() - number - 1);
    }

    @Override
    public SwingEntity getLowest() {
        return tree.values().stream().min(Comparator.comparing(SwingEntity::getPrice)).get();
    }

    @Override
    public SwingEntity getHighest() {
        return tree.values().stream().max(Comparator.comparing(SwingEntity::getPrice)).get();
    }

    @Override
    public List<SwingEntity> findNearbySwings(SwingEntity swing,
                                             int steps) {
        List<SwingEntity> result = new ArrayList<>();
        SwingEntity high = swing;
        SwingEntity low = swing;
        for (int i = 0; i < steps; i++) {
            if (high != null) {
                high = tree.higherEntry(high.getTime()) == null
                        ? null
                        : tree.higherEntry(high.getTime()).getValue();
            }
            if (high != null) {
                high = tree.higherEntry(high.getTime()) == null
                        ? null
                        : tree.higherEntry(high.getTime()).getValue();
            }
            if (high != null) {
                result.add(high);
            }

            if (low != null) {
                low = tree.lowerEntry(low.getTime()) == null
                        ? null
                        : tree.lowerEntry(low.getTime()).getValue();
            }
            if (low != null) {
                low = tree.lowerEntry(low.getTime()) == null
                        ? null
                        : tree.lowerEntry(low.getTime()).getValue();
            }
            if (low != null) {
                result.add(low);
            }
        }
        return result.stream().sorted(Comparator.comparing(SwingEntity::getTime)).collect(Collectors.toList());
    }

}
