package org.trade.rateslib.data.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.slf4j.Logger;
import org.trade.rateslib.model.Timeframe;
import org.trade.rateslib.utils.TimeUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileStorageVolumeRepository {
    private final Path directory;
    private final String stock;
    private final Timeframe timeframe;
    private final Logger log;
    private final LoadingCache<StorageBlock, List<Volume>> cache;

    public FileStorageVolumeRepository(Path directory,
                                       String stock,
                                       Timeframe timeframe,
                                       Logger log) {
        this.directory = directory;
        this.stock = stock;
        this.timeframe = timeframe;
        this.log = log;
        this.cache = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build(this::loadFile);
    }

    public List<Volume> findAllByTimeBetween(LocalDateTime timeStart, LocalDateTime timeEnd) {
        StorageBlock block = getBlockByTime(timeStart);
        List<Volume> result = new ArrayList<>(loadFile(block));
        LocalDateTime start = TimeUtils.plus(block.getEnd(), timeframe);
        while (!start.isAfter(timeEnd)) {
            block = getBlockByTime(start);
            result.addAll(loadFile(block));
            start = TimeUtils.plus(block.getEnd(), timeframe);
        }
        return result.stream().sorted(Comparator.comparing(Volume::time)).collect(Collectors.toList());
    }

    public void update(Volume volume) {
        StorageBlock block = getBlockByTime(volume.time());
        Map<LocalDateTime, Volume> rates = new HashMap<>(loadFile(block).stream().collect(Collectors.toMap(
                Volume::time,
                Function.identity()
        )));
        rates.remove(volume.time());
        rates.put(volume.time(), volume);
        List<Volume> result = new ArrayList<>(rates.values());
        saveFile(block, result);
    }
    
    
    static class StorageBlock {
        private final Path path;
        private final LocalDateTime start;
        private final LocalDateTime end;
        private final int size;

        public StorageBlock(Path path,
                            LocalDateTime start,
                            LocalDateTime end,
                            int size) {
            this.path = path;
            this.start = start;
            this.end = end;
            this.size = size;
        }

        public Path getPath() {
            return path;
        }

        public LocalDateTime getStart() {
            return start;
        }

        public LocalDateTime getEnd() {
            return end;
        }

        public int getSize() {
            return size;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StorageBlock that = (StorageBlock) o;
            return path.equals(that.path) && start.equals(that.start) && end.equals(that.end);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path, start, end);
        }

        @Override
        public String toString() {
            return "StorageBlock{" +
                    "path=" + path +
                    ", start=" + start +
                    ", end=" + end +
                    ", size=" + size +
                    '}';
        }
    }

    List<Volume> loadFile(StorageBlock block) {
        log.debug("loadFile(): block={}", block);
        if (!block.getPath().toFile().exists()) {
            return Collections.emptyList();
        }
        List<Volume> rates = new ArrayList<>();
        try {
            byte[] bytes = Files.readAllBytes(block.getPath());
            DoubleBuffer doubleBuffer = ByteBuffer.wrap(bytes).asDoubleBuffer();
            int limit = doubleBuffer.limit();
            LocalDateTime time = block.getStart();
            for (int i = 0; i < limit; i++) {
                Volume volume = new Volume(doubleBuffer.get(), time);
                rates.add(volume);
                time = time.plus(timeframe.getDuration().getPeriod()).plus(timeframe.getDuration().getDuration());
            }
            return rates;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void saveFile(StorageBlock block, List<Volume> rates) {
        cache.invalidate(block);
        log.debug("saveFile(): block={}, volumes.size={}", block, rates.size());
        Map<LocalDateTime, Volume> ratesToSave = rates.stream().collect(Collectors.toMap(
                Volume::time,
                Function.identity()));
        if (!block.getPath().toFile().exists()) {
            try {
                Files.createDirectories(block.getPath().getParent());
                block.getPath().toFile().createNewFile();
            } catch (IOException e) {
                log.warn("Error on create file: block={}", block, e);
                throw new RuntimeException(e);
            }
        }
        try (FileOutputStream fos = new FileOutputStream(block.getPath().toFile())) {
            ByteBuffer bb = ByteBuffer.allocate(block.getSize() * 8);
            for (LocalDateTime start = block.getStart(); !start.isAfter(block.getEnd()); start = TimeUtils.plus(start, timeframe)) {
                if (ratesToSave.containsKey(start)) {
                    Volume r = ratesToSave.get(start);
                    bb.putDouble(r.value());
                } else {
                    bb.putDouble(0);
                }
            }
            fos.write(bb.array());
        } catch (Exception e) {
            log.warn("Error on write file: block={}", block, e);
        }
    }
    
    private LocalDateTime getFirstWeekDayOfYear(int year) {
        LocalDateTime firstDayOfYear = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        if (firstDayOfYear.getDayOfWeek().getValue() == 1) {
            return firstDayOfYear;
        }
        int weekDiff = 8 - firstDayOfYear.getDayOfWeek().getValue();
        return firstDayOfYear.plusDays(weekDiff);
    }

    StorageBlock getBlockByTime(LocalDateTime time) {
        Path base = directory.resolve(stock).resolve(timeframe.getCode());
        int year = time.getYear();
        int month = time.getMonthValue();
        int day = time.getDayOfMonth();
        switch (timeframe) {
            case MN1:
                int startYear = (int) (time.getYear() / 10.) * 10;
                int endYear = startYear + 9;
                return new StorageBlock(
                        base.resolve(String.format("%d", startYear)),
                        LocalDateTime.of(startYear, 1, 1, 0, 0, 0),
                        LocalDateTime.of(endYear, 12, 1, 0, 0, 0),
                        120);
            case W1:
                return new StorageBlock(
                        base.resolve(String.format("%d", year)),
                        getFirstWeekDayOfYear(year),
                        getFirstWeekDayOfYear(year + 1).minusWeeks(1),
                        53);
            case D1:
                return new StorageBlock(
                        base.resolve(String.format("%d", year)).resolve(String.format("%d", month)),
                        LocalDateTime.of(year, month, 1, 0, 0, 0),
                        LocalDateTime.of(year, month, 1, 0, 0, 0).plusMonths(1).minusDays(1),
                        31);
            case H1:
                return new StorageBlock(
                        base.resolve(String.format("%d", year)).resolve(String.format("%d", month)).resolve(String.format("%d", day)),
                        LocalDateTime.of(year, month, day, 0, 0, 0),
                        LocalDateTime.of(year, month, day, 0, 0, 0).plusDays(1).minusHours(1),
                        24);
            case M15:
                return new StorageBlock(
                        base.resolve(String.format("%d", year)).resolve(String.format("%d", month)).resolve(String.format("%d", day)),
                        LocalDateTime.of(year, month, day, 0, 0, 0),
                        LocalDateTime.of(year, month, day, 0, 0, 0).plusDays(1).minusMinutes(15),
                        96);

            default:
                throw new RuntimeException("Unexpected timeframe: " + timeframe);
        }
    }

    public record Volume(Double value,
                         LocalDateTime time) {

    }
}
