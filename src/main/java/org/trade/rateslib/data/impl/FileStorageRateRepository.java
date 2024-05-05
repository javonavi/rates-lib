package org.trade.rateslib.data.impl;

import org.slf4j.Logger;
import org.trade.rateslib.data.RateEntity;
import org.trade.rateslib.data.RateRepository;
import org.trade.rateslib.model.Timeframe;
import org.trade.rateslib.utils.TimeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Репозиторий котировок, хранящийся в файлах
 *
 * @author Ivanov Andrey
 * @since 23.02.2024
 */
public class FileStorageRateRepository implements RateRepository {

    private final Path directory;
    private final String stock;
    private final Timeframe timeframe;
    private final Logger log;

    public FileStorageRateRepository(Path directory,
                                     String stock,
                                     Timeframe timeframe,
                                     Logger log) {
        this.directory = directory;
        this.stock = stock;
        this.timeframe = timeframe;
        this.log = log;
    }

    @Override
    public RateEntity findFirstByOrderByTimeDesc() {
        return getLatest(1).get(0);
    }

    @Override
    public int countByTimeGreaterThanEqual(LocalDateTime time) {
        return findAllByTimeBetween(time, LocalDateTime.now()).size();
    }

    @Override
    public RateEntity getByIndex(int index) {
        return getLatest(index + 1).get(0);
    }

    @Override
    public List<RateEntity> findAllByTimeBetween(LocalDateTime timeStart, LocalDateTime timeEnd) {
        List<RateEntity> result = new ArrayList<>();
        StorageBlock block = getBlockByTime(timeStart);
        result.addAll(loadFile(block).stream().filter(r -> !r.getTime().isAfter(timeEnd)).collect(Collectors.toList()));
        LocalDateTime start = TimeUtils.plus(block.getEnd(), timeframe);
        while (!start.isAfter(timeEnd)) {
            block = getBlockByTime(timeStart);
            result.addAll(loadFile(block).stream().filter(r -> !r.getTime().isAfter(timeEnd)).collect(Collectors.toList()));
            start = TimeUtils.plus(block.getEnd(), timeframe);
        }
        return result.stream().sorted(Comparator.comparing(RateEntity::getTime)).collect(Collectors.toList());
    }

    @Override
    public int countByTimeBetween(LocalDateTime timeStart, LocalDateTime timeEnd) {
        return findAllByTimeBetween(timeStart, timeEnd).size();
    }

    @Override
    public RateEntity findFirstByTimeLessThanOrderByTimeDesc(LocalDateTime time) {
        throw new RuntimeException("Unsupported method: findFirstByTimeLessThanOrderByTimeDesc");
    }

    @Override
    public List<RateEntity> findAllByTimeGreaterThanEqualOrderByTimeAsc(LocalDateTime time) {
        throw new RuntimeException("Unsupported method: findAllByTimeGreaterThanEqualOrderByTimeAsc");
    }

    @Override
    public void insert(RateEntity rateEntity) {
        StorageBlock block = getBlockByTime(rateEntity.getTime());
        List<RateEntity> rates = loadFile(block);
        if (rates.stream().anyMatch(r -> r.getTime().equals(rateEntity.getTime()))) {
            throw new RuntimeException("Rate already exists: rate=" + rateEntity);
        }
        List<RateEntity> result = new ArrayList<>();
        saveFile(block, result);
    }

    @Override
    public void update(RateEntity rateEntity) {

    }

    @Override
    public Optional<RateEntity> findById(LocalDateTime time) {
        StorageBlock block = getBlockByTime(time);
        if (!block.getPath().toFile().exists()) {
            return Optional.empty();
        }
        return loadFile(block).stream().filter(r -> r.getTime().equals(time)).findFirst();
    }

    @Override
    public int count() {
        throw new RuntimeException("Unsupported method: count");
    }

    @Override
    public void deleteAll() {
        throw new RuntimeException("Unsupported method: deleteAll");
    }

    @Override
    public List<RateEntity> getLatest(int count) {
        Optional<StorageBlock> latestBlock = getLatestBlock();
        if (!latestBlock.isPresent()) {
            return Collections.emptyList();
        }
        List<RateEntity> result = new ArrayList<>();
        result.addAll(loadFile(latestBlock.get()));
        LocalDateTime end = TimeUtils.minus(latestBlock.get().getStart(), timeframe);
        while (result.size() < count) {
            StorageBlock block = getBlockByTime(end);
            if (!block.getPath().toFile().exists()) {
                break;
            }
            result.addAll(loadFile(block));
            end = TimeUtils.minus(block.getStart(), timeframe);
        }
        return result.stream()
                .sorted(Comparator.comparing(RateEntity::getTime, Comparator.reverseOrder()))
                .limit(count)
                .sorted(Comparator.comparing(RateEntity::getTime, Comparator.naturalOrder()))
                .collect(Collectors.toList());
    }

    private StorageBlock getBlockByTime(LocalDateTime time) {
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

            default:
                throw new RuntimeException("Unexpected timeframe: " + timeframe);
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

    private Optional<StorageBlock> getLatestBlock() {
        Path base = directory.resolve(stock).resolve(timeframe.getCode());
        List<String> l = getLatestFileRecursive(base);
        if (l.isEmpty()) {
            return Optional.empty();
        }
        if (l.size() == 1) {
            return Optional.of(getBlockByTime(LocalDateTime.of(Integer.parseInt(l.get(0)), 1, 1, 0, 0)));
        } else if (l.size() == 2) {
            return Optional.of(getBlockByTime(LocalDateTime.of(Integer.parseInt(l.get(0)), Integer.parseInt(l.get(1)), 1, 0, 0)));
        } else if (l.size() == 3) {
            return Optional.of(getBlockByTime(LocalDateTime.of(Integer.parseInt(l.get(0)), Integer.parseInt(l.get(1)), Integer.parseInt(l.get(2)), 0, 0)));
        }
        return Optional.empty();
    }

    private StorageBlock getBlockBefore(StorageBlock block) {
        return getBlockByTime(block.getStart().minusMinutes(1));
    }

    private List<String> getLatestFileRecursive(Path base) {
        List<String> result = new ArrayList<>();
        Path dir = base;
        for (int i = 0; i < 10; i++) {
            Optional<File> f = getLatestFile(dir);
            if (!f.isPresent()) {
                break;
            }
            result.add(f.get().getName());
            if (f.get().isFile()) {
                break;
            }
            dir = dir.resolve(f.get().getName());
        }
        return result;
    }

    private Optional<File> getLatestFile(Path base) {
        File[] files = base.toFile().listFiles();
        if (files == null) {
            return Optional.empty();
        }
        return Stream.of(files)
                .sorted(Comparator.comparing(File::getName).reversed())
                .findFirst();
    }


    @Override
    public List<RateEntity> getLatest(LocalDateTime beforeTime, int limit) {
        List<RateEntity> result = new ArrayList<>();
        StorageBlock block = getBlockByTime(beforeTime);
        result.addAll(loadFile(block));
        while (result.size() < limit) {
            block = getBlockBefore(block);
            if (!block.getPath().toFile().exists()) {
                break;
            }
            List<RateEntity> loadedRates = loadFile(block);
            if (loadedRates.isEmpty()) {
                break;
            }
            result.addAll(loadedRates);
        }
        return result.stream()
                .sorted(Comparator.comparing(RateEntity::getTime, Comparator.reverseOrder()))
                .limit(limit)
                .sorted(Comparator.comparing(RateEntity::getTime, Comparator.naturalOrder()))
                .collect(Collectors.toList());
    }

    private static class StorageBlock {
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

    private List<RateEntity> loadFile(StorageBlock block) {
        if (!block.getPath().toFile().exists()) {
            return Collections.emptyList();
        }
        List<RateEntity> rates = new ArrayList<>();
        try {
            byte[] bytes = Files.readAllBytes(block.getPath());
            DoubleBuffer doubleBuffer = ByteBuffer.wrap(bytes).asDoubleBuffer();
            int limit = doubleBuffer.limit() >> 2;
            LocalDateTime time = block.getStart();
            for (int i = 0; i < limit; i++) {
                RateEntity rate = new RateEntity();
                rate.setTime(time);
                rate.setOpen(doubleBuffer.get());
                rate.setHigh(doubleBuffer.get());
                rate.setLow(doubleBuffer.get());
                rate.setClose(doubleBuffer.get());
                if (Double.compare(rate.getOpen(), 0) > 0) {
                    rates.add(rate);
                }
                time = time.plus(timeframe.getDuration().getPeriod()).plus(timeframe.getDuration().getDuration());
            }
            return rates;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveFile(StorageBlock block, List<RateEntity> rates) {
        Map<LocalDateTime, RateEntity> ratesToSave = rates.stream().collect(Collectors.toMap(
                RateEntity::getTime,
                Function.identity()));
        try (FileOutputStream fos = new FileOutputStream(block.getPath().toFile())) {
            ByteBuffer bb = ByteBuffer.allocate(block.getSize() * 4 * 8);
            for (LocalDateTime start = block.getStart(); !start.isAfter(block.getEnd()); start = TimeUtils.plus(start, timeframe)) {
                if (ratesToSave.containsKey(start)) {
                    RateEntity r = ratesToSave.get(start);
                    bb.putDouble(r.getOpen());
                    bb.putDouble(r.getHigh());
                    bb.putDouble(r.getLow());
                    bb.putDouble(r.getClose());
                } else {
                    bb.putDouble(0);
                    bb.putDouble(0);
                    bb.putDouble(0);
                    bb.putDouble(0);
                }
            }
            fos.write(bb.array());
        } catch (Exception e) {
            log.warn("Error on write file: block={}", block, e);
        }
    }
}
