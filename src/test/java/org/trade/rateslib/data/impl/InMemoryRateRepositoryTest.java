package org.trade.rateslib.data.impl;

import org.junit.jupiter.api.Test;
import org.trade.rateslib.data.RateEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * @author javonavi
 */
public class InMemoryRateRepositoryTest {

    @Test
    public void findFirstByOrderByTimeDesc() {
        InMemoryRateRepository repository = init(List.of(
                LocalDateTime.parse("1999-01-01T00:00:00"),
                LocalDateTime.parse("2000-01-01T00:00:00"),
                LocalDateTime.parse("1999-10-01T00:00:00")
        ));
        assertEquals(repository.findFirstByOrderByTimeDesc().getTime(), LocalDateTime.parse("2000-01-01T00:00:00"));
    }

    @Test
    public void countByTimeGreaterThanEqual() {
        InMemoryRateRepository repository = init(List.of(
                LocalDateTime.parse("1999-01-01T00:00:00"),
                LocalDateTime.parse("2000-01-01T00:00:00"),
                LocalDateTime.parse("1999-10-01T00:00:00"),
                LocalDateTime.parse("2001-01-01T00:00:00"),
                LocalDateTime.parse("2002-01-01T00:00:00")
        ));
        assertEquals(repository.countByTimeGreaterThanEqual(LocalDateTime.parse("2000-01-01T00:00:00")), 3);
    }

    @Test
    public void getByIndex() {
        InMemoryRateRepository repository = init(List.of(
                LocalDateTime.parse("1999-01-01T00:00:00"),
                LocalDateTime.parse("2000-01-01T00:00:00"),
                LocalDateTime.parse("1999-10-01T00:00:00"),
                LocalDateTime.parse("2001-01-01T00:00:00"),
                LocalDateTime.parse("2002-01-01T00:00:00")
        ));
        assertEquals(repository.getByIndex(0).getTime(), LocalDateTime.parse("2002-01-01T00:00:00"));
        assertEquals(repository.getByIndex(2).getTime(), LocalDateTime.parse("2000-01-01T00:00:00"));
        assertEquals(repository.getByIndex(3).getTime(), LocalDateTime.parse("1999-10-01T00:00:00"));
    }

    @Test
    public void findAllByTimeBetween() {
        InMemoryRateRepository repository = init(List.of(
                LocalDateTime.parse("1999-01-01T00:00:00"),
                LocalDateTime.parse("2000-01-01T00:00:00"),
                LocalDateTime.parse("1999-10-01T00:00:00"),
                LocalDateTime.parse("2001-01-01T00:00:00"),
                LocalDateTime.parse("2002-01-01T00:00:00")
        ));
        assertEquals(repository.findAllByTimeBetween(LocalDateTime.parse("1999-10-01T00:00:00"), LocalDateTime.parse("2001-01-01T00:00:00")).size(), 3);
    }

    @Test
    public void findFirstByTimeLessThanOrderByTimeDesc() {
        InMemoryRateRepository repository = init(List.of(
                LocalDateTime.parse("1999-01-01T00:00:00"),
                LocalDateTime.parse("2000-01-01T00:00:00"),
                LocalDateTime.parse("1999-10-01T00:00:00"),
                LocalDateTime.parse("2001-01-01T00:00:00"),
                LocalDateTime.parse("2002-01-01T00:00:00")
        ));
        assertEquals(repository.findFirstByTimeLessThanOrderByTimeDesc(LocalDateTime.parse("2001-01-01T00:00:00")).getTime(), LocalDateTime.parse("2000-01-01T00:00:00"));
        assertEquals(repository.findFirstByTimeLessThanOrderByTimeDesc(LocalDateTime.parse("2000-01-01T00:00:00")).getTime(), LocalDateTime.parse("1999-10-01T00:00:00"));
    }

    @Test
    public void findAllByTimeGreaterThanEqualOrderByTimeAsc() {
        InMemoryRateRepository repository = init(List.of(
                LocalDateTime.parse("1999-01-01T00:00:00"),
                LocalDateTime.parse("2000-01-01T00:00:00"),
                LocalDateTime.parse("1999-10-01T00:00:00"),
                LocalDateTime.parse("2001-01-01T00:00:00"),
                LocalDateTime.parse("2002-01-01T00:00:00")
        ));
        assertEquals(repository.findAllByTimeGreaterThanEqualOrderByTimeAsc(LocalDateTime.parse("2000-01-01T00:00:00")).size(), 3);
        assertEquals(repository.findAllByTimeGreaterThanEqualOrderByTimeAsc(LocalDateTime.parse("1999-10-01T00:00:00")).size(), 4);
    }

    @Test
    public void getLatest() {
        InMemoryRateRepository repository = init(List.of(
                LocalDateTime.parse("1999-01-01T00:00:00"),
                LocalDateTime.parse("2000-01-01T00:00:00"),
                LocalDateTime.parse("1999-10-01T00:00:00"),
                LocalDateTime.parse("2001-01-01T00:00:00"),
                LocalDateTime.parse("2002-01-01T00:00:00")
        ));
        assertEquals(repository.getLatest(LocalDateTime.parse("2001-01-01T00:00:00"), 2).get(0).getTime(), LocalDateTime.parse("2000-01-01T00:00:00"));
        assertEquals(repository.getLatest(LocalDateTime.parse("2001-01-01T00:00:00"), 2).get(1).getTime(), LocalDateTime.parse("1999-10-01T00:00:00"));
    }

    private InMemoryRateRepository init(List<LocalDateTime> times) {
        InMemoryRateRepository repository = new InMemoryRateRepository();
        for (LocalDateTime time : times) {
            RateEntity entity = new RateEntity();
            entity.setClose(1.);
            entity.setHigh(1.);
            entity.setOpen(1.);
            entity.setLow(1.);
            entity.setTime(time);
            repository.insert(entity);
        }
        return repository;
    }
}
