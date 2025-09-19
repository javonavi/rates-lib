package org.trade.rateslib.data.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.slf4j.Logger;
import org.trade.rateslib.data.RateEntity;
import org.trade.rateslib.data.RateRepository;
import org.trade.rateslib.model.Timeframe;
import org.trade.rateslib.utils.TimeUtils;

import java.io.File;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
    private final LoadingCache<StorageBlock, List<RateEntity>> cache;

    public FileStorageRateRepository(Path directory,
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
        List<RateEntity> list = getLatest(index + 1);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<RateEntity> findAllByTimeBetween(LocalDateTime timeStart, LocalDateTime timeEnd) {
        StorageBlock block = getBlockByTime(timeStart);
        Set<RateEntity> result = loadFile(block).stream()
                .filter(r -> !r.getTime().isBefore(timeStart))
                .filter(r -> !r.getTime().isAfter(timeEnd))
                .collect(Collectors.toSet());
        LocalDateTime start = TimeUtils.plus(block.getEnd(), timeframe);
        while (!start.isAfter(timeEnd)) {
            block = getBlockByTime(start);
            result.addAll(loadFile(block).stream()
                    .filter(r -> !r.getTime().isAfter(timeEnd))
                    .filter(r -> !r.getTime().isBefore(timeStart))
                    .collect(Collectors.toSet()));
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
        List<RateEntity> result = new ArrayList<>(rates);
        result.add(rateEntity);
        saveFile(block, result);
    }

    @Override
    public void update(RateEntity rateEntity) {
        StorageBlock block = getBlockByTime(rateEntity.getTime());
        Map<LocalDateTime, RateEntity> rates = loadFile(block).stream().collect(Collectors.toMap(
             RateEntity::getTime,
             Function.identity()
        ));
        rates.remove(rateEntity.getTime());
        rates.put(rateEntity.getTime(), rateEntity);
        List<RateEntity> result = new ArrayList<>(rates.values());
        saveFile(block, result);
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
        if (latestBlock.isEmpty()) {
            return Collections.emptyList();
        }
        List<RateEntity> result = new ArrayList<>(loadFile(latestBlock.get()));
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
            StorageBlock block1 = getBlockByTime(LocalDateTime.of(Integer.parseInt(l.get(0)), 1, 1, 0, 0));
            return Optional.of(block1.getPath().toFile().length() == 0
                    ? getBlockBefore(block1)
                    : block1);
        } else if (l.size() == 2) {
            StorageBlock block2 = getBlockByTime(LocalDateTime.of(Integer.parseInt(l.get(0)), Integer.parseInt(l.get(1)), 1, 0, 0));
            return Optional.of(block2.getPath().toFile().length() == 0
                    ? getBlockBefore(block2)
                    : block2);
        } else if (l.size() == 3) {
            StorageBlock block3 = getBlockByTime(LocalDateTime.of(Integer.parseInt(l.get(0)), Integer.parseInt(l.get(1)), Integer.parseInt(l.get(2)), 0, 0));
            return Optional.of(block3.getPath().toFile().length() == 0
                    ? getBlockBefore(block3)
                    : block3);
        }
        return Optional.empty();
    }

    private StorageBlock getBlockBefore(StorageBlock block) {
        return getBlockByTime(TimeUtils.minus(block.getStart(), timeframe));
    }

    private List<String> getLatestFileRecursive(Path base) {
        List<String> result = new ArrayList<>();
        Path dir = base;
        for (int i = 0; i < 10; i++) {
            Optional<File> f = getLatestFile(dir);
            if (f.isEmpty()) {
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
                .map(File::getName)
                .map(Integer::parseInt)
                .max(Integer::compare)
                .map(i -> base.resolve(i.toString()))
                .map(Path::toFile);
    }


    @Override
    public List<RateEntity> getLatest(LocalDateTime beforeTime, int limit) {
        StorageBlock block = getBlockByTime(beforeTime);
        List<RateEntity> result = loadFile(block).stream().filter(r -> r.getTime().isBefore(beforeTime))
                .collect(Collectors.toList());
        int emptyBlocksCount = 0;
        while (result.size() < limit) {
            block = getBlockBefore(block);
            if (!block.getPath().toFile().exists()) {
                emptyBlocksCount++;
                if (result.isEmpty() && emptyBlocksCount <= 100) {
                    continue;
                } else if (emptyBlocksCount <= 7) {
                    continue;
                } else {
                    break;
                }
            }
            List<RateEntity> loadedRates = loadFile(block);
            if (loadedRates.isEmpty()) {
                break;
            }
            emptyBlocksCount = 0;
            result.addAll(loadedRates.stream().filter(r -> r.getTime().isBefore(beforeTime)).toList());
        }
        return result.stream()
                .sorted(Comparator.comparing(RateEntity::getTime, Comparator.reverseOrder()))
                .limit(limit)
                .sorted(Comparator.comparing(RateEntity::getTime, Comparator.naturalOrder()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RateEntity> getFirstRate() {
        Path base = directory.resolve(stock).resolve(timeframe.getCode());
        switch (timeframe) {
            case MN1:
                var path0 = findFirstFile(base);
                if (path0.isEmpty()) return Optional.empty();
                LocalDateTime time0 = getFirstWeekDayOfYear(Integer.parseInt(path0.get().getFileName().toString()));
                return loadFile(getBlockByTime(time0)).stream().min(Comparator.comparing(RateEntity::getTime)).stream().findFirst();
            case W1:
                var path1 = findFirstFile(base);
                if (path1.isEmpty()) return Optional.empty();
                LocalDateTime time1 = LocalDateTime.of(Integer.parseInt(path1.get().getFileName().toString()), 1, 1, 0, 0);
                return loadFile(getBlockByTime(time1)).stream().min(Comparator.comparing(RateEntity::getTime)).stream().findFirst();
            case D1:
                var path2 = findFirstFile(base);
                if (path2.isEmpty()) return Optional.empty();
                var path3 = findFirstFile(path2.get());
                if (path3.isEmpty()) return Optional.empty();
                LocalDateTime time2 = LocalDateTime.of(
                        Integer.parseInt(path2.get().getFileName().toString()),
                        Integer.parseInt(path3.get().getFileName().toString()), 1, 0, 0);
                return loadFile(getBlockByTime(time2)).stream().min(Comparator.comparing(RateEntity::getTime)).stream().findFirst();
            case H1:
            case M15:
                var path4 = findFirstFile(base);
                if (path4.isEmpty()) return Optional.empty();
                var path5 = findFirstFile(path4.get());
                if (path5.isEmpty()) return Optional.empty();
                var path6 = findFirstFile(path5.get());
                if (path6.isEmpty()) return Optional.empty();
                LocalDateTime time3 = LocalDateTime.of(
                        Integer.parseInt(path4.get().getFileName().toString()),
                        Integer.parseInt(path5.get().getFileName().toString()),
                        Integer.parseInt(path6.get().getFileName().toString()), 0, 0);
                return loadFile(getBlockByTime(time3)).stream().min(Comparator.comparing(RateEntity::getTime)).stream().findFirst();

            default:
                throw new RuntimeException("Unexpected timeframe: " + timeframe);

        }
    }

    private Optional<Path> findFirstFile(Path path) {
        try (var stream = Files.list(path)) {
            return stream.sorted().findFirst();
        } catch (IOException e) {
            log.warn("Error on list directory: path={}", path);
            return Optional.empty();
        }
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

    List<RateEntity> loadFile(StorageBlock block) {
        log.debug("loadFile(): block={}", block);
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

    void saveFile(StorageBlock block, List<RateEntity> rates) {
        cache.invalidate(block);
        log.debug("saveFile(): block={}, rates.size={}", block, rates.size());
        Map<LocalDateTime, RateEntity> ratesToSave = rates.stream().collect(Collectors.toMap(
                RateEntity::getTime,
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
