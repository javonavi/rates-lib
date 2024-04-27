package org.trade.rateslib.data.impl;

import org.junit.jupiter.api.Test;
import org.trade.rateslib.data.SwingEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author javonavi
 */
public class InMemorySwingRepositoryTest {

    @Test
    public void deleteByTimeGreaterThan() {
        InMemorySwingRepository repository = init(List.of(
                LocalDateTime.parse("1999-01-01T00:00:00"),
                LocalDateTime.parse("2000-01-01T00:00:00"),
                LocalDateTime.parse("1999-10-01T00:00:00")
        ));
        repository.deleteByTimeGreaterThan(LocalDateTime.parse("1999-10-01T00:00:00"));
        assertEquals(repository.count(), 2);
    }

    @Test
    public void countByTimeGreaterThanEqual() {
        InMemorySwingRepository repository = init(List.of(
                LocalDateTime.parse("1999-01-01T00:00:00"),
                LocalDateTime.parse("2000-01-01T00:00:00"),
                LocalDateTime.parse("1999-10-01T00:00:00"),
                LocalDateTime.parse("2001-01-01T00:00:00"),
                LocalDateTime.parse("2002-01-01T00:00:00")
        ));
        assertEquals(repository.findBeforeTime(LocalDateTime.parse("2000-01-01T00:00:00")).get().getTime(), LocalDateTime.parse("1999-10-01T00:00:00"));
        assertEquals(repository.findBeforeTime(LocalDateTime.parse("2001-01-01T00:00:00")).get().getTime(), LocalDateTime.parse("2000-01-01T00:00:00"));
        assertTrue(repository.findBeforeTime(LocalDateTime.parse("1999-01-01T00:00:00")).isEmpty());
    }

    @Test
    public void getByIndex() {
        InMemorySwingRepository repository = init(List.of(
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
        InMemorySwingRepository repository = init(List.of(
                LocalDateTime.parse("1999-01-01T00:00:00"),
                LocalDateTime.parse("2000-01-01T00:00:00"),
                LocalDateTime.parse("1999-10-01T00:00:00"),
                LocalDateTime.parse("2001-01-01T00:00:00"),
                LocalDateTime.parse("2002-01-01T00:00:00")
        ));
        assertEquals(repository.findAllByTimeBetween(LocalDateTime.parse("1999-10-01T00:00:00"), LocalDateTime.parse("2001-01-01T00:00:00")).size(), 3);
    }

    private InMemorySwingRepository init(List<LocalDateTime> times) {
        InMemorySwingRepository repository = new InMemorySwingRepository();
        for (LocalDateTime time : times) {
            SwingEntity entity = new SwingEntity();
            entity.setTime(time);
            repository.save(entity);
        }
        return repository;
    }

}
