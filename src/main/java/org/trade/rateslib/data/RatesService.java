package org.trade.rateslib.data;

import org.trade.rateslib.model.Rate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * @author javonavi
 */
public class RatesService {
    private final Map<String, Map<String, RateRepository>> ratesRepository;

    public RatesService() {
        this.ratesRepository = new HashMap<>();
    }

    public synchronized void init(String stock, Map<String, RateRepository> rateRepositoryMap) {
        this.ratesRepository.put(stock, rateRepositoryMap);
    }

    public List<RateEntity> mapRates(Collection<Rate> rates, String stock, String timeframe) {
        return rates.stream()
                .map(rate -> {
                    RateEntity entity = createClass(stock, timeframe);
                    entity.setTime(rate.getTime());
                    entity.setHigh(rate.getHigh().doubleValue());
                    entity.setLow(rate.getLow().doubleValue());
                    entity.setOpen(rate.getOpen().doubleValue());
                    entity.setClose(rate.getClose().doubleValue());
                    return entity;
                })
                .collect(Collectors.toList());
    }

    public Rate mapRateEntity(RateEntity rateEntity, String timeframe) {
        return Rate.builder()
                .withTime(rateEntity.getTime())
                .withTimeframe(timeframe.toUpperCase())
                .withHigh(rateEntity.getHigh())
                .withLow(rateEntity.getLow())
                .withClose(rateEntity.getClose())
                .withOpen(rateEntity.getOpen())
                .build();
    }

    private RateEntity createClass(String stock, String timeframe) {
        return new RateEntity();
    }

    public void insertOrIgnore(String stock,
                               String timeframe,
                               Collection<RateEntity> rates) {
        RateRepository repository = getRepository(stock, timeframe);
        rates.forEach(r -> {
            if (repository.findById(r.getTime()).isEmpty()) {
                repository.insert(r);
            }
        });
    }

    public void insertOrUpdate(String stock,
                               String timeframe,
                               Collection<RateEntity> rates) {
        RateRepository repository = getRepository(stock, timeframe);
        rates.forEach(r -> {
            if (repository.findById(r.getTime()).isPresent()) {
                repository.update(r);
            } else {
                repository.insert(r);
            }
        });
    }

    private RateRepository getRepository(String stock,
                                         String timeframe) {
        return ratesRepository.get(stock).get(timeframe.toLowerCase());
    }

    public Optional<RateEntity> getByTime(LocalDateTime time,
                                          String stock,
                                          String timeframe) {
        requireNonNull(time, "time");
        requireNonNull(stock, "stock");
        requireNonNull(timeframe, "timeframe");
        return getRepository(stock, timeframe).findById(time);
    }

    public Optional<LocalDateTime> getLatestTime(String stock,
                                                 String timeframe) {
        requireNonNull(stock, "stock");
        requireNonNull(timeframe, "timeframe");
        RateEntity entity = getRepository(stock, timeframe).findFirstByOrderByTimeDesc();
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(entity.getTime());
    }

    /**
     * Возвращает индекс бара в коллекции
     *
     * @param stock
     * @param timeframe
     * @param time
     * @return Если не найдено, возвращает -1
     */
    public int getShift(String stock, String timeframe, LocalDateTime time) {
        return getRepository(stock, timeframe).countByTimeGreaterThanEqual(time) - 1;
    }

    /**
     * Возвращает бар по его индексу
     *
     * @param stock
     * @param timeframe
     * @param index
     * @return
     */
    public Optional<RateEntity> getRate(String stock, String timeframe, int index) {
        return Optional.ofNullable(getRepository(stock, timeframe).getByIndex(index));
    }

    public Optional<RateEntity> getLatestRate(String stock, String timeframe) {
        return getRate(stock, timeframe, 0);
    }

    public Optional<RateEntity> getLatestRateBeforeTime(String stock, String timeframe, LocalDateTime beforeTime) {
        return Optional.ofNullable(getRepository(stock, timeframe).findFirstByTimeLessThanOrderByTimeDesc(beforeTime));
    }

    public Optional<RateEntity> getHighestRate(String stock, String timeframe, LocalDateTime fromTime, LocalDateTime toTime) {
        return getRepository(stock, timeframe).findAllByTimeBetween(fromTime, toTime)
                .stream().max(Comparator.comparing(RateEntity::getHigh));
    }

    public Optional<RateEntity> getLowestRate(String stock, String timeframe, LocalDateTime fromTime, LocalDateTime toTime) {
        return getRepository(stock, timeframe).findAllByTimeBetween(fromTime, toTime)
                .stream().min(Comparator.comparing(RateEntity::getLow));
    }

    public List<RateEntity> getRatesFromTime(String stock, String timeframe, LocalDateTime fromTime) {
        return getRepository(stock, timeframe).findAllByTimeGreaterThanEqualOrderByTimeAsc(fromTime);
    }

    /**
     * Возвращает количество баров
     *
     * @param stock
     * @param timeframe
     * @return
     */
    public int getCount(String stock, String timeframe) {
        return (int) getRepository(stock, timeframe).count();
    }

    /**
     * Возвращает количество баров между датами
     *
     * @param stock
     * @param timeframe
     * @param fromTime
     * @param toTime
     * @return
     */
    public int getCountBetween(String stock, String timeframe, LocalDateTime fromTime, LocalDateTime toTime) {
        return (int) getRepository(stock, timeframe).countByTimeBetween(fromTime, toTime);
    }

    public List<RateEntity> getRatesBetween(String stock, String timeframe, LocalDateTime fromTime, LocalDateTime toTime) {
        return getRepository(stock, timeframe).findAllByTimeBetween(fromTime, toTime);
    }

    public void deleteAll(String stock, String timeframe) {
        getRepository(stock, timeframe).deleteAll();
    }

    public List<RateEntity> getLatest(String stock,
                                      String timeframe,
                                      int count) {
        return getRepository(stock, timeframe).getLatest(count);
    }

    public List<RateEntity> getLatest(String stock,
                                      String timeframe,
                                      int count,
                                      LocalDateTime beforeTime) {
        if (nonNull(beforeTime)) {
            return getRepository(stock, timeframe).getLatest(count);
        }
        return getRepository(stock, timeframe).getLatest(beforeTime, count);
    }
}
