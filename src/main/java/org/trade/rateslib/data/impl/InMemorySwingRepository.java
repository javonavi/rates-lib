package org.trade.rateslib.data.impl;

import org.trade.rateslib.data.SwingEntity;
import org.trade.rateslib.data.SwingRepository;
import org.trade.rateslib.model.SwingPoint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.lang.Math.max;

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
        List<SwingEntity> result = list.subList(max(list.size() - count - 1, 0), list.size() - 1);
        Collections.reverse(result);
        return result;
    }

    @Override
    public Optional<Integer> getShift(LocalDateTime time) {
        List<LocalDateTime> list = new ArrayList<>(tree.keySet());
        Collections.sort(list, Collections.reverseOrder());
        int index = list.indexOf(time);
        return index == -1 ? Optional.empty() : Optional.of(index);
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
