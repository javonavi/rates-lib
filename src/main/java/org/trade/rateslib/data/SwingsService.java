package org.trade.rateslib.data;

import org.trade.rateslib.model.SwingDirection;
import org.trade.rateslib.model.SwingPoint;
import org.trade.rateslib.model.Timeframe;
import org.trade.rateslib.utils.TimeframeProvider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.util.Objects.requireNonNull;

/**
 * @author javonavi
 */
public class SwingsService {
    private final Map<String, Map<String, SwingRepository>> swingsRepository;

    public SwingsService() {
        this.swingsRepository = new HashMap<>();
    }

    public synchronized void init(String stock, SwingRepository repository) {
        this.swingsRepository.put(stock, new HashMap<>());
        this.swingsRepository.get(stock).put("m5", repository);
        this.swingsRepository.get(stock).put("m15", repository);
        this.swingsRepository.get(stock).put("h1", repository);
        this.swingsRepository.get(stock).put("h4", repository);
        this.swingsRepository.get(stock).put("d1", repository);
        this.swingsRepository.get(stock).put("w1", repository);
        this.swingsRepository.get(stock).put("mn1", repository);
    }

    /**
     * Удаляет все свинги начиная с определенного времени
     *
     * @param stock
     * @param timeframe
     * @param time
     */
    public void deleteSwingsAfterTime(String stock, String timeframe, LocalDateTime time) {
        getRepository(stock, timeframe).deleteByTimeGreaterThan(time);
    }

    private SwingRepository getRepository(String stock,
                                          String timeframe) {
        return swingsRepository.get(stock).get(timeframe.toLowerCase());
    }

    /**
     * Возвращает количество свингов
     *
     * @param stock
     * @param timeframe
     * @return
     */
    public int getCount(String stock, String timeframe) {
        return (int) getRepository(stock, timeframe).count();
    }

    /**
     * Возвращает свинг по его индексу
     *
     * @param stock
     * @param timeframe
     * @param index
     * @return
     */
    public Optional<SwingEntity> getSwing(String stock, String timeframe, int index) {
        return Optional.ofNullable(getRepository(stock, timeframe).getByIndex(index));
    }

    public void save(String stock, String timeframe, SwingPoint swingPoint) {
        SwingEntity entity = createClass(stock, timeframe);
        entity.setDirection(SwingDirection.DOWN == swingPoint.getDirection());
        entity.setLength(swingPoint.getLength());
        entity.setLengthInBars(swingPoint.getLengthInBars());
        entity.setPrice(swingPoint.getPrice().doubleValue());
        entity.setTime(swingPoint.getTime());
        getRepository(stock, timeframe).save(entity);
    }

    public SwingEntity createClass(String stock, String timeframe) {
        return new SwingEntity();
    }

    /**
     * Находит наиболее мелкий свинг для текущего
     * Если не находит, возвращает текущий
     *
     * @param stock      Товар
     * @param swingPoint Текущий свинг, для которого производим поиск
     * @return
     */
    public SwingPoint findPreciseSwing(String stock, SwingPoint swingPoint) {
        SwingDirection direction = swingPoint.getDirection();
        SwingPoint foundedSwing = SwingPoint.builder().fromSwingPoint(swingPoint).build();
        Optional<String> previousTimeframe = TimeframeProvider.getPrevious(foundedSwing.getTimeframe());
        while (previousTimeframe.isPresent()) {
            LocalDateTime startTime = swingPoint.getTime().minusMinutes(Timeframe.valueOf(previousTimeframe.get().toUpperCase()).getValue());
            LocalDateTime endTime = swingPoint.getTime().plusMinutes(Timeframe.valueOf(previousTimeframe.get().toUpperCase()).getValue());
            List<? extends SwingEntity> swings = getRepository(stock, previousTimeframe.get())
                    .findAllByTimeBetween(startTime, endTime);
            if (swings.isEmpty()) {
                return foundedSwing;
            }
            double swingPrice = foundedSwing.getPrice().doubleValue();
            Optional<? extends SwingEntity> closestSwingEntity = swings.stream()
                    .filter(swing -> SwingDirection.byBoolean(swing.getDirection()) == direction)
                    .min(new Comparator<SwingEntity>() {
                        @Override
                        public int compare(SwingEntity o1, SwingEntity o2) {
                            return Double.compare(
                                    abs(o1.getPrice() - swingPrice),
                                    abs(o2.getPrice() - swingPrice));
                        }
                    });
            if (closestSwingEntity.isEmpty()) {
                return foundedSwing;
            }
            foundedSwing = convertEntityToSwing(closestSwingEntity.get(), previousTimeframe.get());
            previousTimeframe = TimeframeProvider.getPrevious(foundedSwing.getTimeframe());
        }
        return foundedSwing;
    }

    /**
     * Возвращае предыдущий свинг
     *
     * @param stock
     * @param timeframe
     * @param time
     * @return
     */
    public Optional<SwingEntity> findSwingBeforeTime(String stock, String timeframe, LocalDateTime time) {
        Optional<SwingEntity> swing = getRepository(stock, timeframe).findBeforeTime(time);
        return swing;
    }

    public SwingPoint convertEntityToSwing(SwingEntity entity, String timeframe) {
        return SwingPoint.builder()
                .withDirection(entity.getDirection() ? SwingDirection.DOWN : SwingDirection.UP)
                .withPrice(BigDecimal.valueOf(entity.getPrice()))
                .withSection(0)
                .withTime(entity.getTime())
                .withTimeframe(timeframe)
                .build();
    }

    public Optional<SwingPoint> findNearestSwing(String stock,
                                                 String timeframe,
                                                 LocalDateTime time,
                                                 double price,
                                                 boolean swingDirection) {
        requireNonNull(stock, "stock");
        requireNonNull(timeframe, "timeframe");
        requireNonNull(time, "time");

        Timeframe currentTimeframe = Timeframe.valueOf(timeframe.toUpperCase());
        LocalDateTime fromTime = time.minusMinutes(currentTimeframe.getValue());
        LocalDateTime toTime = time.plusMinutes(currentTimeframe.getValue());
        while (true) {
            Optional<SwingEntity> swingEntity = getRepository(stock, currentTimeframe.getCode().toLowerCase())
                    .findAllByTimeBetween(fromTime, toTime).stream()
                    .filter(swing -> ((SwingEntity) swing).getDirection() == swingDirection)
                    .min(new Comparator() {
                        @Override
                        public int compare(Object o1, Object o2) {
                            SwingEntity s1 = (SwingEntity) o1;
                            SwingEntity s2 = (SwingEntity) o2;
                            return Double.compare(abs(s1.getPrice() - price), abs(s2.getPrice() - price));
                        }
                    });
            if (swingEntity.isPresent()) {
                return swingEntity.map(se -> convertEntityToSwing(se, timeframe));
            }
            if (currentTimeframe.getPrev().isEmpty()) {
                break;
            }
            currentTimeframe = currentTimeframe.getPrev().get();
        }
        return Optional.empty();
    }

    public List<SwingPoint> findSwingsBetween(String stock,
                                              String timeframe,
                                              LocalDateTime fromTime,
                                              LocalDateTime toTime) {
        requireNonNull(stock, "stock");
        requireNonNull(timeframe, "timeframe");
        requireNonNull(fromTime, "fromTime");
        requireNonNull(toTime, "toTime");

        List<SwingEntity> swingEntities = getRepository(stock, timeframe).findAllByTimeBetween(fromTime, toTime);
        return swingEntities.stream()
                .map(se -> convertEntityToSwing(se, timeframe))
                .collect(Collectors.toList());
    }

    public Optional<SwingPoint> getSwingByTime(String stock, String timeframe, LocalDateTime time) {
        Optional<SwingEntity> swingPoint = Optional.ofNullable(getRepository(stock, timeframe).findByTime(time));
        return swingPoint.map(se -> convertEntityToSwing(se, timeframe));
    }
}
