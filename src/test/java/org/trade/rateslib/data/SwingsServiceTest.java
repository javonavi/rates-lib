package org.trade.rateslib.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trade.rateslib.data.impl.InMemorySwingRepository;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SwingsServiceTest {

    private static final String STOCK = "TEST";

    private SwingsService swingsService;
    private InMemorySwingRepository repository;

    @BeforeEach
    public void init() {
        swingsService = new SwingsService();
        repository = new InMemorySwingRepository();
        swingsService.init(STOCK, Map.of("d1", repository));
    }

    @Test
    public void findExtremeSwingUpReturnsLowestPrice() {
        LocalDateTime time = LocalDateTime.parse("2026-07-13T00:00:00");
        saveSwing(LocalDateTime.parse("2026-07-13T00:00:00"), true, 22.86);
        saveSwing(LocalDateTime.parse("2026-07-15T00:00:00"), true, 21.5);
        saveSwing(LocalDateTime.parse("2026-07-17T00:00:00"), true, 19.115);
        saveSwing(LocalDateTime.parse("2026-07-16T00:00:00"), false, 24.9);

        var swing = swingsService.findExtremeSwing(STOCK, "D1", time, true);

        assertTrue(swing.isPresent());
        assertEquals(19.115, swing.get().getPrice().doubleValue(), 1e-9);
        assertEquals(LocalDateTime.parse("2026-07-17T00:00:00"), swing.get().getTime());
    }

    @Test
    public void findExtremeSwingDownReturnsHighestPrice() {
        LocalDateTime time = LocalDateTime.parse("2026-03-09T00:00:00");
        saveSwing(LocalDateTime.parse("2026-03-09T00:00:00"), false, 46.0);
        saveSwing(LocalDateTime.parse("2026-03-11T00:00:00"), false, 46.465);
        saveSwing(LocalDateTime.parse("2026-03-13T00:00:00"), false, 45.95);
        saveSwing(LocalDateTime.parse("2026-03-12T00:00:00"), true, 42.95);

        var swing = swingsService.findExtremeSwing(STOCK, "D1", time, false);

        assertTrue(swing.isPresent());
        assertEquals(46.465, swing.get().getPrice().doubleValue(), 1e-9);
    }

    @Test
    public void findExtremeSwingIgnoresSwingsOutsideWindow() {
        LocalDateTime time = LocalDateTime.parse("2026-07-13T00:00:00");
        saveSwing(LocalDateTime.parse("2026-07-13T00:00:00"), true, 22.86);
        saveSwing(LocalDateTime.parse("2026-07-04T00:00:00"), true, 18.0);
        saveSwing(LocalDateTime.parse("2026-07-21T00:00:00"), true, 17.0);

        var swing = swingsService.findExtremeSwing(STOCK, "D1", time, true);

        assertTrue(swing.isPresent());
        assertEquals(22.86, swing.get().getPrice().doubleValue(), 1e-9);
    }

    @Test
    public void findExtremeSwingTieBreakByNearestTime() {
        LocalDateTime time = LocalDateTime.parse("2026-07-13T00:00:00");
        saveSwing(LocalDateTime.parse("2026-07-15T00:00:00"), true, 19.115);
        saveSwing(LocalDateTime.parse("2026-07-17T00:00:00"), true, 19.115);

        var swing = swingsService.findExtremeSwing(STOCK, "D1", time, true);

        assertTrue(swing.isPresent());
        assertEquals(LocalDateTime.parse("2026-07-15T00:00:00"), swing.get().getTime());
    }

    private void saveSwing(LocalDateTime time, boolean direction, double price) {
        SwingEntity entity = new SwingEntity();
        entity.setTime(time);
        entity.setDirection(direction);
        entity.setPrice(price);
        entity.setLength(0.);
        entity.setLengthInBars(0.);
        repository.save(entity);
    }
}
