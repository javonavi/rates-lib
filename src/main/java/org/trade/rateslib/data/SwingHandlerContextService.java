package org.trade.rateslib.data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author javonavi
 */
public class SwingHandlerContextService {
    private final Map<String, Map<String, SwingHandlerContextRepository>> repository;

    public SwingHandlerContextService() {
        this.repository = new HashMap<>();
    }

    public synchronized void init(String stock, Supplier<SwingHandlerContextRepository> repositorySupplier) {
        this.repository.put(stock, new HashMap<>());
        this.repository.get(stock).put("m5", repositorySupplier.get());
        this.repository.get(stock).put("m15", repositorySupplier.get());
        this.repository.get(stock).put("h1", repositorySupplier.get());
        this.repository.get(stock).put("h4", repositorySupplier.get());
        this.repository.get(stock).put("d1", repositorySupplier.get());
        this.repository.get(stock).put("w1", repositorySupplier.get());
        this.repository.get(stock).put("mn1", repositorySupplier.get());
        this.repository.get(stock).put("y1", repositorySupplier.get());
    }

    /**
     * Удаляет все контексты начиная с определенного времени (времени бара, на котором был сформирован контекст)
     *
     * @param stock
     * @param timeframe
     * @param time
     */
    public void deleteAfterTime(String stock, String timeframe, LocalDateTime time) {
        getRepository(stock, timeframe).deleteByTimeGreaterThan(time);
    }

    private SwingHandlerContextRepository getRepository(String stock,
                                                        String timeframe) {
        return repository.get(stock).get(timeframe);
    }

    public void save(String stock,
                     String timeframe,
                     SwingHandlerContextEntity entity) {
        getRepository(stock, timeframe).save(entity);
    }

    public Optional<SwingHandlerContextEntity> getLatestContextBeforeTime(String stock, String timeframe,
                                                                          LocalDateTime time) {
        List<SwingHandlerContextEntity> contexts =
                getRepository(stock, timeframe).findAllByTimeLessThan(time, PageRequest.of(0, 1, Sort.by("time").descending()));
        return contexts.isEmpty()
                ? Optional.empty()
                : Optional.of(contexts.get(0));
    }

    public SwingHandlerContextEntity createEntityObject(String stock, String timeframe) {
        return new SwingHandlerContextEntity();
    }
}
