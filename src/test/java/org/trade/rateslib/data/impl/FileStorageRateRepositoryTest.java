package org.trade.rateslib.data.impl;

import org.junit.jupiter.api.Test;
import org.slf4j.impl.StaticLoggerBinder;
import org.trade.rateslib.data.RateEntity;
import org.trade.rateslib.model.Timeframe;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileStorageRateRepositoryTest {

    private static final Path path = Paths.get("tmp");

    @Test
    public void test_get_latest() {
        Timeframe timeframe = Timeframe.D1;
        FileStorageRateRepository repository = new FileStorageRateRepository(path, "SBER", timeframe, StaticLoggerBinder.getSingleton().getLoggerFactory().getLogger("test"));
        List<LocalDateTime> times = Arrays.asList(
                LocalDateTime.of(2021, 1, 2, 0, 0),
                LocalDateTime.of(2021, 1, 3, 0, 0),
                LocalDateTime.of(2021, 1, 4, 0, 0),
                LocalDateTime.of(2021, 1, 7, 0, 0),
                LocalDateTime.of(2021, 1, 8, 0, 0)
        );
        times.forEach(t -> insertRate(repository, t));
        List<RateEntity> rates = repository.getLatest(5);
        assertEquals(rates.size(), 5);
    }

    private RateEntity getRate(LocalDateTime time) {
        RateEntity r = new RateEntity();
        r.setLow(10.);
        r.setHigh(10.);
        r.setOpen(5.);
        r.setClose(5.);
        r.setTime(time);
        return r;
    }

    private void insertRate(FileStorageRateRepository repository,
                            LocalDateTime time) {
        repository.update(getRate(time));
    }

    @Test
    public void test_insert_load() {
        Timeframe timeframe = Timeframe.MN1;
        FileStorageRateRepository repository = new FileStorageRateRepository(path, "SBER", timeframe, StaticLoggerBinder.getSingleton().getLoggerFactory().getLogger("test"));
        List<LocalDateTime> times = Arrays.asList(
                LocalDateTime.of(2021, 1, 1, 0, 0),
                LocalDateTime.of(2021, 2, 1, 0, 0),
                LocalDateTime.of(2021, 3, 1, 0, 0),
                LocalDateTime.of(2021, 4, 1, 0, 0),
                LocalDateTime.of(2021, 5, 1, 0, 0)
        );
        FileStorageRateRepository.StorageBlock block = repository.getBlockByTime(times.get(0));
        if (block.getPath().toFile().exists()) {
            block.getPath().toFile().delete();
        }
        repository.saveFile(block, times.stream().map(this::getRate).collect(Collectors.toList()));
        List<RateEntity> rates = repository.loadFile(block);
        rates.forEach(System.out::println);
    }
}
