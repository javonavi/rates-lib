package org.trade.rateslib.model;

import org.slf4j.Logger;
import org.trade.rateslib.data.RateEntity;
import org.trade.rateslib.data.RatesService;
import org.trade.rateslib.data.SwingEntity;
import org.trade.rateslib.data.SwingHandlerContextEntity;
import org.trade.rateslib.data.SwingsService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static java.lang.Math.abs;
import static java.lang.Math.max;

/**
 * @author javonavi
 */
public class SwingsHandler {

    private static final Boolean UP = true;
    private static final Boolean DOWN = false;

    private SwingHandlerContextEntity context;
    private final int reverseBarsCount;
    private final String timeframe;
    private final String stock;
    private final RatesService ratesStorage;
    private final SwingsService swingsStorage;
    private final Logger log;

    public SwingsHandler(int reverseBarsCount,
                         String timeframe,
                         String stock,
                         SwingHandlerContextEntity swingHandlerContextEntity,
                         RatesService ratesStorage,
                         SwingsService swingsStorage,
                         Logger logger) {
        this.timeframe = timeframe;
        this.stock = stock;
        this.context = swingHandlerContextEntity;
        this.reverseBarsCount = reverseBarsCount;
        this.context.setReverseBarsCount(reverseBarsCount);
        this.ratesStorage = ratesStorage;
        this.swingsStorage = swingsStorage;
        this.log = logger;
    }

    public Integer getReverseBarsCount() {
        return reverseBarsCount;
    }

    public void setContext(SwingHandlerContextEntity context) {
        this.context = context;
    }

    public SwingHandlerContextEntity getContext() {
        return context;
    }

    public synchronized Optional<SwingPoint> addRate(Rate rate) {
        Objects.requireNonNull(rate, "rate is null");
        log.debug("Add rate: rate={}", rate);

        boolean debug = false;//LocalDateTime.parse("2021-01-08T06:00").equals(rate.getTime());

        if (debug) {
            System.out.println("Debug is enabled: " + rate.getTime());
        }

        Optional<SwingPoint> result = Optional.empty();

        if (context.getLocalHigh() == null) {
            context.setLocalHigh(rate.getHigh().doubleValue());
            context.setCurrentHigh(rate.getHigh().doubleValue());
        }
        if (context.getLocalLow() == null) {
            context.setLocalLow(rate.getLow().doubleValue());
            context.setCurrentLow(rate.getLow().doubleValue());
        }

        boolean alreadyReverse = false;

        if (context.getLastDownSwing() != null && context.getLastUpSwing() != null) {
            if (UP == context.getCurrentDirection()
                    && Double.compare(rate.getLow().doubleValue(), context.getLastDownSwing()) < 0
                    && Double.compare(rate.getHigh().doubleValue(), context.getLastUpSwing()) < 0) {
                correctNewLastWorkingPoint(DOWN, rate);
                result = reverse(rate.getTime(), DOWN, "byLastSwing DOWN", ratesStorage);
                context.setLastWorkingPoint(rate.getTime());
                context.setCurrentLow(rate.getLow().doubleValue());
                alreadyReverse = true;
            } else if (DOWN == context.getCurrentDirection()
                    && Double.compare(rate.getHigh().doubleValue(), context.getLastUpSwing()) > 0
                    && Double.compare(rate.getLow().doubleValue(), context.getLastDownSwing()) > 0) {
                correctNewLastWorkingPoint(UP, rate);
                result = reverse(rate.getTime(), UP, "byLastSwing UP", ratesStorage);
                context.setLastWorkingPoint(rate.getTime());
                context.setCurrentHigh(rate.getHigh().doubleValue());
                alreadyReverse = true;
            }
        }

        // Обновили оба максимума
        if (!alreadyReverse && Double.compare(rate.getHigh().doubleValue(), context.getLocalHigh()) > 0 &&
                Double.compare(rate.getLow().doubleValue(), context.getLocalLow()) < 0 && context.getCurrentDirection() != null) {
            log.trace("Update extremums: context={}", context);
            if ((UP == context.getCurrentDirection() && context.getCurrentHigh().compareTo(rate.getHigh().doubleValue()) < 0 ||
                    (DOWN == context.getCurrentDirection() && context.getCurrentLow().compareTo(rate.getLow().doubleValue()) > 0))) {
                context.setLastWorkingPoint(rate.getTime());
                log.trace("Set last working point: type=1.1, time={}", rate.getTime());
            }

            if (UP == context.getCurrentDirection() && context.getCurrentHigh().compareTo(rate.getHigh().doubleValue()) < 0) {
                if (!(context.getGlobalLow().compareTo(rate.getLow().doubleValue()) > 0 && rate.getClose().compareTo(rate.getOpen()) < 0) &&
                        context.getCurrentHigh().compareTo(rate.getHigh().doubleValue()) < 0) {
                    if (context.getLastWorkingPrice() == null || Double.compare(rate.getHigh().doubleValue(), context.getLastWorkingPrice()) > 0) {
                        context.setLastWorkingPoint(rate.getTime());
                        context.setLastWorkingPrice(rate.getHigh().doubleValue());
                        log.trace("Set last working point: type=1.2, time={}", rate.getTime());
                    }
                    context.setCurrentHigh(rate.getHigh().doubleValue());
                }
            } else if (DOWN == context.getCurrentDirection() && context.getCurrentLow().compareTo(rate.getLow().doubleValue()) > 0) {
                if (context.getGlobalHigh() != null &&
                        !(context.getGlobalHigh().compareTo(rate.getHigh().doubleValue()) < 0 &&
                                rate.getClose().compareTo(rate.getOpen()) > 0) &&
                        context.getCurrentLow().compareTo(rate.getLow().doubleValue()) > 0) {
                    if (context.getLastWorkingPrice() == null || Double.compare(rate.getLow().doubleValue(), context.getLastWorkingPrice()) < 0) {
                        context.setLastWorkingPoint(rate.getTime());
                        context.setLastWorkingPrice(rate.getLow().doubleValue());
                        log.trace("Set last working point: type=1.3, time={}", rate.getTime());
                    }
                    context.setCurrentLow(rate.getLow().doubleValue());
                }
            } else if ((UP == context.getCurrentDirection() && context.getCurrentHigh().compareTo(rate.getHigh().doubleValue()) < 0) ||
                    (DOWN == context.getCurrentDirection() && context.getCurrentLow().compareTo(rate.getLow().doubleValue()) > 0)) {
                setLastWorkingPrice(rate);
                context.setLastWorkingPoint(rate.getTime());
                log.trace("Set last working point: type=1.4, time={}", rate.getTime());
            }

            context.setWaitingReverseCount(context.getReverseBarsCount() + 1);

            if (context.getReverseBarsCount() >= reverseBarsCount) {
                if ((UP == context.getCurrentDirection() && !(context.getGlobalHigh().compareTo(rate.getHigh().doubleValue()) > 0)) ||
                        (DOWN == context.getCurrentDirection() && !(context.getGlobalLow().compareTo(rate.getLow().doubleValue()) < 0))) {
                    setLastWorkingPrice(rate);
                    context.setLastWorkingPoint(rate.getTime());
                    log.trace("Set last working point: type=1.5, time={}", rate.getTime());
                }
            }

            if (UP == context.getCurrentDirection() && rate.getClose().compareTo(rate.getOpen()) < 0 &&
                    context.getCurrentHigh().compareTo(rate.getHigh().doubleValue()) < 0) {
                setLastWorkingPrice(rate);
                context.setLastWorkingPoint(rate.getTime());
                log.trace("Set last working point: type=1.6, time={}", rate.getTime());
                context.setCurrentHigh(rate.getHigh().doubleValue());
            } else if (DOWN == context.getCurrentDirection() && rate.getClose().compareTo(rate.getOpen()) > 0 &&
                    context.getCurrentLow().compareTo(rate.getLow().doubleValue()) > 0) {
                setLastWorkingPrice(rate);
                context.setLastWorkingPoint(rate.getTime());
                log.trace("Set last working point: type=1.7, time={}", rate.getTime());
                context.setCurrentLow(rate.getLow().doubleValue());
            }

            context.setCurrentHigh(max(rate.getHigh().doubleValue(), context.getCurrentHigh()));
            context.setCurrentLow(max(rate.getLow().doubleValue(), context.getCurrentLow()));
        }
        // Обновили глобальный максимум
        else if (!alreadyReverse && context.getGlobalHigh() != null && Double.compare(rate.getHigh().doubleValue(), context.getGlobalHigh()) > 0 &&
                DOWN == context.getCurrentDirection() && !(context.getCurrentLow().compareTo(rate.getLow().doubleValue()) > 0)) {
            log.trace("Update globalHigh");
            result = reverse(rate.getTime(), UP, "Update global MAX", ratesStorage);
            context.setLastWorkingPoint(rate.getTime());
            log.trace("Set last working point: type=2.1, time={}", rate.getTime());
            alreadyReverse = true;
            context.setCurrentHigh(rate.getHigh().doubleValue());
        }
        // Обновили глобальный минимум
        else if (!alreadyReverse && context.getGlobalLow() != null && Double.compare(rate.getLow().doubleValue(), context.getGlobalLow()) < 0 &&
                UP == context.getCurrentDirection() && !(context.getCurrentHigh().compareTo(rate.getHigh().doubleValue()) < 0)) {
            log.trace("Update globalLow");
            result = reverse(rate.getTime(), DOWN, "Update global MIN", ratesStorage);
            context.setLastWorkingPoint(rate.getTime());
            log.trace("Set last working point: type=3.1, time={}", rate.getTime());
            alreadyReverse = true;
            context.setCurrentLow(rate.getLow().doubleValue());
        }
        // Обновили только локальный максимум
        else if (!alreadyReverse && Double.compare(rate.getHigh().doubleValue(), context.getLocalHigh()) > 0 &&
                !(Double.compare(rate.getLow().doubleValue(), context.getLocalLow()) < 0)) {
            log.trace("Update LocalHigh");

            if (UP == context.getCurrentDirection()) {
                if (Double.compare(rate.getHigh().doubleValue(), context.getCurrentHigh()) > 0) {
                    //if (ratesStorage.getRate(context.getLastWorkingPoint(), context.getTimeframe())
                    //        .filter(r -> r.getHigh().compareTo(rate.getHigh()) > 0).isPresent()) {
                    context.setWaitingReverseCount(0);
                    setLastWorkingPrice(rate);
                    context.setLastWorkingPoint(rate.getTime());
                    log.trace("Set last working point: type=4.1, time={}", rate.getTime());
                    //}
                    context.setCurrentHigh(rate.getHigh().doubleValue());
                }
            } else if (DOWN == context.getCurrentDirection()) {
                log.trace("incWaitingReverseCount");
                context.incWaitingReverseCount();
                if (context.getWaitingReverseCount() >= reverseBarsCount) {
                    result = reverse(rate.getTime(), UP, "waitingReverseCount UP", ratesStorage);
                    alreadyReverse = true;
                } else if (context.getLastWorkingPrice() != null && context.getLastDownSwing() != null &&
                        (context.getLastDownSwing() - context.getLastWorkingPrice()) * 0.5
                                <= rate.getHigh().doubleValue() - context.getLastWorkingPrice()
                        && context.getWaitingReverseCount() >= reverseBarsCount - 1) {
                    int movingSize = ratesStorage.getShift(stock, timeframe, context.getLastWorkingPoint()) -
                            ratesStorage.getShift(stock, timeframe, rate.getTime());
                    if (movingSize > 1 && swingsStorage.getCount(stock, timeframe) > 1) {
                        int swingsCount = swingsStorage.getCount(stock, timeframe);
                        Optional<SwingEntity> swing1 = swingsStorage.getSwing(stock, timeframe, swingsCount - 1);
                        Optional<SwingEntity> swing2 = swingsStorage.getSwing(stock, timeframe, swingsCount - 2);
                        if (swing1.isPresent() && swing2.isPresent()) {
                            double swingSize = abs(swing1.get().getPrice() - context.getLastWorkingPrice());
                            double diff = abs(rate.getLow().doubleValue() - swing2.get().getPrice());
                            if (swingSize > 0 && diff / swingSize < 0.2) {
                                result = reverse(rate.getTime(), UP, "fastMoving UP", ratesStorage);
                                alreadyReverse = true;
                            }
                        }
                    }
                }
            } else {
                context.setCurrentDirection(UP);
            }
        }
        // Обновили только локальный минимум
        else if (!alreadyReverse && !(Double.compare(rate.getHigh().doubleValue(), context.getLocalHigh()) > 0) &&
                Double.compare(rate.getLow().doubleValue(), context.getLocalLow()) < 0) {
            log.trace("Update LocalLow");

            if (DOWN == context.getCurrentDirection()) {
                if (Double.compare(rate.getLow().doubleValue(), context.getCurrentLow()) < 0) {
                    //if (ratesStorage.getRate(context.getLastWorkingPoint(), context.getTimeframe())
                    //        .filter(r -> r.getLow().compareTo(rate.getLow()) < 0).isPresent()) {
                    context.setWaitingReverseCount(0);
                    setLastWorkingPrice(rate);
                    context.setLastWorkingPoint(rate.getTime());
                    log.trace("Set last working point: type=5.1, time={}", rate.getTime());
                    //}
                    context.setCurrentLow(rate.getLow().doubleValue());
                }
            } else if (UP == context.getCurrentDirection()) {
                log.trace("incWaitingReverseCount");
                context.incWaitingReverseCount();
                if (context.getWaitingReverseCount() >= reverseBarsCount) {
                    result = reverse(rate.getTime(), DOWN, "waitingReverseCount DOWN", ratesStorage);
                    alreadyReverse = true;
                } else if (context.getLastWorkingPrice() != null && context.getLastUpSwing() != null &&
                        (context.getLastWorkingPrice() - context.getLastUpSwing()) * 0.5
                                <= context.getLastWorkingPrice() - rate.getLow().doubleValue()
                        && context.getWaitingReverseCount() >= reverseBarsCount - 1) {
                    int movingSize = ratesStorage.getShift(stock, timeframe, context.getLastWorkingPoint()) -
                            ratesStorage.getShift(stock, timeframe, rate.getTime());
                    if (movingSize > 1 && swingsStorage.getCount(stock, timeframe) > 1) {
                        int swingsCount = swingsStorage.getCount(stock, timeframe);
                        Optional<SwingEntity> swing1 = swingsStorage.getSwing(stock, timeframe, swingsCount - 1);
                        Optional<SwingEntity> swing2 = swingsStorage.getSwing(stock, timeframe, swingsCount - 2);
                        if (swing1.isPresent() && swing2.isPresent()) {
                            double swingSize = abs(swing1.get().getPrice() - context.getLastWorkingPrice());
                            double diff = abs(rate.getLow().doubleValue() - swing2.get().getPrice());
                            if (swingSize > 0 && diff / swingSize < 0.2) {
                                result = reverse(rate.getTime(), DOWN, "fastMoving DOWN", ratesStorage);
                                alreadyReverse = true;
                            }
                        }
                    }
                }
            } else {
                context.setCurrentDirection(DOWN);
            }
        }

        if (!alreadyReverse && DOWN == context.getCurrentDirection()
                && Double.compare(rate.getHigh().doubleValue(), context.getLocalHigh()) > 0) {
            Optional<RateEntity> prevRate = ratesStorage.getRate(stock, timeframe, reverseBarsCount);
            if (prevRate.isPresent()
                    && ratesStorage.getHighestRate(stock, timeframe, prevRate.get().getTime(), rate.getTime()).filter(r -> r.getTime().equals(rate.getTime())).isPresent()
                    && ratesStorage.getLowestRate(stock, timeframe, prevRate.get().getTime(), rate.getTime()).filter(r -> r.getTime().equals(rate.getTime())).isEmpty()) {
                result = reverse(rate.getTime(), UP, "barsUpCount", ratesStorage);
                alreadyReverse = true;
            }
        }

        if (!alreadyReverse && UP == context.getCurrentDirection()
                && Double.compare(rate.getLow().doubleValue(), context.getLocalLow()) < 0) {
            Optional<RateEntity> prevRate = ratesStorage.getRate(stock, timeframe, reverseBarsCount);
            if (prevRate.isPresent()
                    && ratesStorage.getLowestRate(stock, timeframe, prevRate.get().getTime(), rate.getTime()).filter(r -> r.getTime().equals(rate.getTime())).isPresent()
                    && ratesStorage.getHighestRate(stock, timeframe, prevRate.get().getTime(), rate.getTime()).filter(r -> r.getTime().equals(rate.getTime())).isEmpty()) {
                result = reverse(rate.getTime(), DOWN, "barsDownCount", ratesStorage);
                alreadyReverse = true;
            }
        }

        context.setLocalHigh(rate.getHigh().doubleValue());
        context.setLocalLow(rate.getLow().doubleValue());

        if (!alreadyReverse
                && context.getCurrentDirection() != null
                && ratesStorage.getCount(stock, timeframe) > reverseBarsCount + 10
                && ratesStorage.getLatest(stock, timeframe, reverseBarsCount + 10).size() >= reverseBarsCount + 1) {
            LocalDateTime reverseBarTime = ratesStorage.getLatestRate(stock, timeframe).get().getTime();
            if (UP == context.getCurrentDirection() &&
                    (context.getLastWorkingPoint() != null && !context.getLastWorkingPoint().isAfter(reverseBarTime))) {
                Optional<LocalDateTime> newLastWorkingPoint = checkDownReverseByLastBars();
                if (newLastWorkingPoint.isPresent() && newLastWorkingPoint.get().isAfter(reverseBarTime)) {
                    result = reverse(reverseBarTime, DOWN, "downByLastBars", ratesStorage, newLastWorkingPoint.get());
                    //log.error("result={}", result);
                    alreadyReverse = true;
                }
            }
            if (DOWN == context.getCurrentDirection() &&
                    (context.getLastWorkingPoint() != null && !context.getLastWorkingPoint().isAfter(reverseBarTime))) {
                Optional<LocalDateTime> newLastWorkingPoint = checkUpReverseByLastBars();
                if (newLastWorkingPoint.isPresent() && newLastWorkingPoint.get().isAfter(reverseBarTime)) {
                    result = reverse(reverseBarTime, UP, "upByLastBars", ratesStorage, newLastWorkingPoint.get());
                    //log.error("result={}", result);
                    alreadyReverse = true;
                }
            }
        }

        if (UP == context.getCurrentDirection()) {
            if (context.getGlobalHigh() == null || context.getCurrentHigh().compareTo(context.getGlobalHigh()) > 0) {
                context.setGlobalHigh(context.getCurrentHigh());
            }
        } else {
            if (context.getGlobalLow() == null || context.getCurrentLow().compareTo(context.getGlobalLow()) < 0) {
                context.setGlobalLow(context.getCurrentLow());
            }
        }

        if (debug) System.out.println("result: " + result);

        if (alreadyReverse) {
            return result;
        }

        if (UP == context.getCurrentDirection()) {
            Optional<LocalDateTime> workingPoint = lastBarsGrowDown(ratesStorage);
            if (workingPoint.isPresent()) {
                result = reverse(rate.getTime(), DOWN, "lastBarsGrowDown", ratesStorage);
                alreadyReverse = true;
            }
        } else {
            Optional<LocalDateTime> workingPoint = lastBarsGrowUp(ratesStorage);
            if (workingPoint.isPresent()) {
                result = reverse(rate.getTime(), UP, "lastBarsGrowUp", ratesStorage);
                alreadyReverse = true;
            }
        }

        if (debug) System.out.println("result: " + result);

        return result;
    }

    private void correctNewLastWorkingPoint(boolean direction,
                                            Rate rate) {
        if (context.getLastWorkingPoint() == null) {
            return;
        }
        Optional<RateEntity> extremumRate = direction == UP
                ? ratesStorage.getLowestRate(stock, timeframe, context.getLastWorkingPoint(), rate.getTime())
                : ratesStorage.getHighestRate(stock, timeframe, context.getLastWorkingPoint(), rate.getTime());
        extremumRate.map(RateEntity::getTime).ifPresent(t -> context.setLastWorkingPoint(t));
    }

    Optional<LocalDateTime> checkDownReverseByLastBars() {
        if (ratesStorage.getCount(stock, timeframe) < reverseBarsCount + 1) {
            return Optional.empty();
        }

        RateEntity checkedRate = ratesStorage.getRate(stock, timeframe, reverseBarsCount)
                .orElseThrow();
        double high = checkedRate.getHigh();
        double low = checkedRate.getLow();
        double lowest = low;
        LocalDateTime time = checkedRate.getTime();

        for (int i = 0; i < reverseBarsCount; i++) {
            RateEntity curRate = ratesStorage.getRate(stock, timeframe, i)
                    .orElseThrow();
            double curLow = curRate.getLow();
            double curHigh = curRate.getHigh();
            if (curLow > low || high < curHigh) {
                return Optional.empty();
            }
            if (lowest > curLow) {
                lowest = curLow;
                time = curRate.getTime();
            }
        }

        return Optional.of(time);
    }

    Optional<LocalDateTime> checkUpReverseByLastBars() {
        if (ratesStorage.getCount(stock, timeframe) < reverseBarsCount + 1) {
            return Optional.empty();
        }

        RateEntity checkedRate = ratesStorage.getRate(stock, timeframe, reverseBarsCount)
                .orElseThrow();
        double high = checkedRate.getHigh();
        double low = checkedRate.getLow();
        double highest = high;
        LocalDateTime time = checkedRate.getTime();

        for (int i = 0; i < reverseBarsCount; i++) {
            RateEntity curRate = ratesStorage.getRate(stock, timeframe, i)
                    .orElseThrow();
            double curHigh = curRate.getHigh();
            double curLow = curRate.getLow();
            if (curHigh > high || low > curLow) {
                return Optional.empty();
            }
            if (highest < curHigh) {
                highest = curHigh;
                time = curRate.getTime();
            }
        }

        return Optional.of(time);
    }

    private void setLastWorkingPrice(Rate rate) {
        log.debug("Set last working price");
        if (context.getLastWorkingPrice() == null) {
            context.setLastWorkingPrice(context.getCurrentDirection() == UP ? rate.getHigh().doubleValue() : rate.getLow().doubleValue());
        } else if (context.getCurrentDirection() == UP && Double.compare(rate.getHigh().doubleValue(), context.getLastWorkingPrice()) > 0) {
            context.setLastWorkingPrice(rate.getHigh().doubleValue());
        } else if (context.getCurrentDirection() == DOWN && Double.compare(rate.getLow().doubleValue(), context.getLastWorkingPrice()) < 0) {
            context.setLastWorkingPrice(rate.getLow().doubleValue());
        }
    }

    private Optional<LocalDateTime> lastBarsGrowUp(RatesService ratesStorage) {
        if (ratesStorage.getCount(stock, timeframe) <= reverseBarsCount * 2 + 10) {
            return Optional.empty();
        }
        int barsCount = ratesStorage.getLatest(stock, timeframe, reverseBarsCount * 2 + 10).size();
        if (barsCount <= reverseBarsCount) return Optional.empty();
        RateEntity bar = ratesStorage.getRate(stock, timeframe, 0)
                .orElseThrow();
        double high = bar.getHigh();
        double low = bar.getLow();
        double min = bar.getLow();
        LocalDateTime workingPoint = bar.getTime();

        for (int i = 1; i <= reverseBarsCount; i++) {
            RateEntity prevBar = ratesStorage.getRate(stock, timeframe, i)
                    .orElseThrow();
            if (bar.getHigh() < prevBar.getHigh()) return Optional.empty();
            if (min > prevBar.getLow()) {
                min = prevBar.getLow();
                workingPoint = prevBar.getTime();
            }
            bar = prevBar;
        }
        if (bar.getLow() >= low) return Optional.empty();
        for (int i = reverseBarsCount + 1; i < reverseBarsCount * 2 && i < barsCount; i++) {
            RateEntity prevBar = ratesStorage.getRate(stock, timeframe, i)
                    .orElseThrow();
            if (prevBar.getHigh() >= high) return Optional.empty();
        }
        return Optional.of(workingPoint);
    }

    private Optional<LocalDateTime> lastBarsGrowDown(RatesService ratesStorage) {
        if (ratesStorage.getCount(stock, timeframe) <= reverseBarsCount * 2 + 10) {
            return Optional.empty();
        }
        int barsCount = ratesStorage.getLatest(stock, timeframe, reverseBarsCount * 2 + 10).size();
        if (barsCount <= reverseBarsCount) return Optional.empty();
        RateEntity bar = ratesStorage.getRate(stock, timeframe, 0)
                .orElseThrow();
        double low = bar.getLow();
        double high = bar.getHigh();
        double max = bar.getHigh();
        LocalDateTime workingPoint = bar.getTime();

        for (int i = 1; i <= reverseBarsCount; i++) {
            RateEntity prevBar = ratesStorage.getRate(stock, timeframe, i)
                    .orElseThrow();
            if (bar.getLow() > prevBar.getLow()) return Optional.empty();
            if (max < prevBar.getHigh()) {
                max = prevBar.getHigh();
                workingPoint = prevBar.getTime();
            }
            bar = prevBar;
        }
        if (bar.getHigh() <= high) return Optional.empty();
        for (int i = reverseBarsCount + 1; i < reverseBarsCount * 2 && i < barsCount; i++) {
            RateEntity prevBar = ratesStorage.getRate(stock, timeframe, i)
                    .orElseThrow();
            if (prevBar.getLow() <= low) return Optional.empty();
        }
        return Optional.of(workingPoint);
    }

    private Optional<SwingPoint> reverse(LocalDateTime time, boolean direction, String cause,
                                         RatesService ratesStorage) {
        return reverse(time, direction, cause, ratesStorage, null);
    }

    private Optional<SwingPoint> reverse(LocalDateTime time, boolean direction, String cause,
                                         RatesService ratesStorage, LocalDateTime newLastWorkingPoint) {
        log.debug("Reverse: direction={}, time={}, cause={}, lastWorkingPoint={}, period={}, newLastWorkingPoint={}",
                direction, time, cause, context.getLastWorkingPoint(), timeframe, newLastWorkingPoint);
        Objects.requireNonNull(time, "time is null");

        LocalDateTime prevLastWorkingPoint = context.getLastWorkingPoint();
        if (context.getLastWorkingPoint() == null) {
            //throw new RuntimeException("LastWorkingPoint is null: time=" + time + ", context=" + context);
            context.setLastWorkingPoint(time);
            //return Optional.empty();
        }

        int shift = ratesStorage.getShift(stock, timeframe, context.getLastWorkingPoint());
        if (shift == -1) {
            log.warn("shift=-1: time={}, context.getTimeframe()={}, context.getLastWorkingPoint()={}", time,
                    timeframe, context.getLastWorkingPoint());
        }
        RateEntity rate = ratesStorage.getRate(stock, timeframe, shift)
                .orElseThrow();
        double price = direction == UP ? rate.getLow() : rate.getHigh();
        log.debug("Reverse: price={}, time={}, shift={}, reason={}, reverseTime={}", price, context.getLastWorkingPoint(), shift,
                cause, ratesStorage.getRate(stock, timeframe, 0).map(RateEntity::getTime).orElse(null));
        SwingPoint swing = SwingPoint.builder()
                .withSection(0)
                .withTime(context.getLastWorkingPoint())
                .withTimeframe(timeframe)
                .withDirection(SwingDirection.byBoolean(direction))
                .withPrice(BigDecimal.valueOf(price))
                .build();

        // try to find upper/lower time from reverse
        if (prevLastWorkingPoint == null) {
            log.debug("Set last working point by nullable prevLastWorkingPoint: {}", time);
            context.setLastWorkingPoint(time);
        } else {
            int nextShift = ratesStorage.getShift(stock, timeframe, time);
            if (nextShift == shift) {
                log.debug("Set last working point by shift: {}", time);
                context.setLastWorkingPoint(time);
            }
            //debug(time, "nextShift=" + IntegerToString(nextShift) + "; shift=" + IntegerToString(shift));
            //debug(time, "nextShift=" + TimeToString(iTime(symbol, period, nextShift)) + "; shift=" + TimeToString(iTime(symbol, period, shift)));
            if (direction == UP) {
                ratesStorage.getHighestRate(stock, timeframe, prevLastWorkingPoint, time)
                        .ifPresent(rt -> context.setLastWorkingPoint(rt.getTime()));
                //lastWorkingPoint = iTime(symbol, period, iHighest(symbol, TFMigrate(period), MODE_HIGH, shift - nextShift, nextShift));
                //debug(time, "1: " + IntegerToString(iHighest(symbol, TFMigrate(period), MODE_HIGH, shift - nextShift, nextShift)));
                //debug(time, "1: " + TimeToString(iTime(symbol, period, 50)));
            } else {
                ratesStorage.getLowestRate(stock, timeframe, prevLastWorkingPoint, time)
                        .ifPresent(rt -> context.setLastWorkingPoint(rt.getTime()));
                //lastWorkingPoint = iTime(symbol, period, iLowest(symbol, TFMigrate(period), MODE_LOW, shift - nextShift, nextShift));
                //debug(time, "2: " + IntegerToString(iLowest(symbol, TFMigrate(period), MODE_LOW, shift - nextShift, nextShift)));
                //debug(time, "2: " + TimeToString(iTime(symbol, period, 50)));
            }
        }

        log.debug("Add swing: swing={}", swing);

        context.setWaitingReverseCount(0);
        log.trace("newLastWorkingPoint={}", newLastWorkingPoint);
        context.setLastWorkingPoint(newLastWorkingPoint == null ? time : newLastWorkingPoint);
        context.setCurrentDirection(direction);
        context.setCurrentHigh(price);
        context.setCurrentLow(price);
        context.setGlobalHigh(context.getCurrentHigh());
        context.setGlobalLow(context.getCurrentLow());
        if (direction == DOWN) {
            context.setLastUpSwing(price);
        } else {
            context.setLastDownSwing(price);
        }

        return Optional.of(swing);
    }

    @Override
    public String toString() {
        return "SwingsHandler{" +
                "context=" + context +
                ", reverseBarsCount=" + reverseBarsCount +
                ", timeframe=" + timeframe +
                '}';
    }
}
