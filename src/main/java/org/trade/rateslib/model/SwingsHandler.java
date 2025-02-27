package org.trade.rateslib.model;

import org.slf4j.Logger;
import org.trade.rateslib.data.RateEntity;
import org.trade.rateslib.data.SwingEntity;
import org.trade.rateslib.data.SwingHandlerContextEntity;
import org.trade.rateslib.data.SwingsService;
import org.trade.rateslib.data.impl.InMemoryRateRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    private final InMemoryRateRepository inMemoryRateRepository;
    private final SwingsService swingsStorage;
    private final Logger log;

    public SwingsHandler(int reverseBarsCount,
                         String timeframe,
                         String stock,
                         SwingHandlerContextEntity swingHandlerContextEntity,
                         SwingsService swingsStorage,
                         Logger logger) {
        this.timeframe = timeframe;
        this.stock = stock;
        this.context = swingHandlerContextEntity;
        this.reverseBarsCount = reverseBarsCount;
        this.context.setReverseBarsCount(reverseBarsCount);
        this.inMemoryRateRepository = new InMemoryRateRepository();
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

    public synchronized Optional<SwingPoint> addRate(RateEntity rate) {
        Objects.requireNonNull(rate, "rate is null");
        log.debug("Add rate: rate={}", rate);
        inMemoryRateRepository.insert(rate);
        
        boolean debug = false;//LocalDateTime.parse("2021-01-08T06:00").equals(rate.getTime());

        if (debug) {
            System.out.println("Debug is enabled: " + rate.getTime());
        }

        Optional<SwingPoint> result = Optional.empty();

        if (context.getLocalHigh() == null) {
            context.setLocalHigh(rate.getHigh());
            context.setCurrentHigh(rate.getHigh());
        }
        if (context.getLocalLow() == null) {
            context.setLocalLow(rate.getLow());
            context.setCurrentLow(rate.getLow());
        }

        log.trace("Latest: workingPoint={}, workingPrice={}", context.getLastWorkingPoint(), context.getLastWorkingPrice());

        boolean alreadyReverse = false;

        if (context.getLastDownSwing() != null && context.getLastUpSwing() != null) {
            List<SwingEntity> latest = swingsStorage.getLatest(stock, timeframe, 1);
            if (UP == context.getCurrentDirection()
                    && Double.compare(rate.getLow(), context.getLastDownSwing()) < 0
                    && Double.compare(rate.getHigh(), context.getLastUpSwing()) < 0) {
                correctNewLastWorkingPoint(DOWN, rate);
                if (latest.isEmpty() || !latest.get(0).getTime().isEqual(context.getLastWorkingPoint())) {
                    result = reverse(rate.getTime(), DOWN, "byLastSwing DOWN", inMemoryRateRepository);
                } else {
                    log.debug("Skip reverse by swing: swingTime={}, lastWorkingPoint={}", latest.get(0).getTime(), context.getLastWorkingPoint());
                }
                context.setLastWorkingPoint(rate.getTime());
                context.setCurrentLow(rate.getLow());
                alreadyReverse = true;
            } else if (DOWN == context.getCurrentDirection()
                    && Double.compare(rate.getHigh(), context.getLastUpSwing()) > 0
                    && Double.compare(rate.getLow(), context.getLastDownSwing()) > 0) {
                correctNewLastWorkingPoint(UP, rate);
                if (latest.isEmpty() || !latest.get(0).getTime().isEqual(context.getLastWorkingPoint())) {
                    result = reverse(rate.getTime(), UP, "byLastSwing UP", inMemoryRateRepository);
                } else {
                    log.debug("Skip reverse by swing: swingTime={}, lastWorkingPoint={}", latest.get(0).getTime(), context.getLastWorkingPoint());
                }
                context.setLastWorkingPoint(rate.getTime());
                context.setCurrentHigh(rate.getHigh());
                alreadyReverse = true;
            }
        }

        // Обновили оба максимума
        if (!alreadyReverse && Double.compare(rate.getHigh(), context.getLocalHigh()) > 0 &&
                Double.compare(rate.getLow(), context.getLocalLow()) < 0 && context.getCurrentDirection() != null) {
            log.trace("Update extremums: context={}", context);
            if ((UP == context.getCurrentDirection() && context.getCurrentHigh().compareTo(rate.getHigh()) < 0 ||
                    (DOWN == context.getCurrentDirection() && context.getCurrentLow().compareTo(rate.getLow()) > 0))) {
                context.setLastWorkingPoint(rate.getTime());
                log.trace("Set last working point: type=1.1, time={}", rate.getTime());
            }

            if (UP == context.getCurrentDirection() && Double.compare(context.getCurrentHigh(), rate.getHigh()) < 0) {
                if (!(context.getGlobalLow().compareTo(rate.getLow()) > 0 && rate.getClose().compareTo(rate.getOpen()) < 0) &&
                        context.getCurrentHigh().compareTo(rate.getHigh()) < 0) {
                    if (context.getLastWorkingPrice() == null || Double.compare(rate.getHigh(), context.getLastWorkingPrice()) > 0) {
                        context.setLastWorkingPoint(rate.getTime());
                        context.setLastWorkingPrice(rate.getHigh());
                        log.trace("Set last working point: type=1.2, time={}", rate.getTime());
                    }
                    context.setCurrentHigh(rate.getHigh());
                }
            } else if (DOWN == context.getCurrentDirection() && Double.compare(context.getCurrentLow(), rate.getLow()) > 0) {
                if (context.getGlobalHigh() != null &&
                        !(context.getGlobalHigh().compareTo(rate.getHigh()) < 0 &&
                                rate.getClose().compareTo(rate.getOpen()) > 0) &&
                        context.getCurrentLow().compareTo(rate.getLow()) > 0) {
                    if (context.getLastWorkingPrice() == null || Double.compare(rate.getLow(), context.getLastWorkingPrice()) < 0) {
                        context.setLastWorkingPoint(rate.getTime());
                        context.setLastWorkingPrice(rate.getLow());
                        log.trace("Set last working point: type=1.3, time={}", rate.getTime());
                    }
                    context.setCurrentLow(rate.getLow());
                }
            } else if ((UP == context.getCurrentDirection() && context.getCurrentHigh().compareTo(rate.getHigh()) < 0) ||
                    (DOWN == context.getCurrentDirection() && context.getCurrentLow().compareTo(rate.getLow()) > 0)) {
                setLastWorkingPrice(rate);
                context.setLastWorkingPoint(rate.getTime());
                log.trace("Set last working point: type=1.4, time={}", rate.getTime());
            }

            context.setWaitingReverseCount(context.getReverseBarsCount() + 1);

            if (context.getReverseBarsCount() >= reverseBarsCount) {
                if ((UP == context.getCurrentDirection() && !(context.getGlobalHigh().compareTo(rate.getHigh()) > 0)) ||
                        (DOWN == context.getCurrentDirection() && !(context.getGlobalLow().compareTo(rate.getLow()) < 0))) {
                    log.trace("lastWorkingPoint={}, currentDirection={}", context.getLastWorkingPoint(), context.getCurrentDirection());
                    boolean updateWorkingPoint = true;
                    List<SwingEntity> latest = swingsStorage.getLatest(stock, timeframe, 1);
                    if (!latest.isEmpty() && context.getLastWorkingPoint() != null) {
                        if (UP == context.getCurrentDirection()) {
                            Optional<RateEntity> highest = inMemoryRateRepository.getHighestRate(latest.get(0).getTime(), rate.getTime());
                            if (highest.isPresent() && highest.get().getTime().isEqual(context.getLastWorkingPoint())) {
                                updateWorkingPoint = false;
                            }
                        } else {
                            Optional<RateEntity> lowest = inMemoryRateRepository.getLowestRate(latest.get(0).getTime(), rate.getTime());
                            if (lowest.isPresent() && lowest.get().getTime().isEqual(context.getLastWorkingPoint())) {
                                updateWorkingPoint = false;
                            }
                        }
                    }
                    if (updateWorkingPoint) {
                        setLastWorkingPrice(rate);
                        context.setLastWorkingPoint(rate.getTime());
                        log.trace("Set last working point: type=1.5, time={}", rate.getTime());
                    }
                }
            }

            if (UP == context.getCurrentDirection() && rate.getClose().compareTo(rate.getOpen()) < 0 &&
                    context.getCurrentHigh().compareTo(rate.getHigh()) < 0) {
                setLastWorkingPrice(rate);
                context.setLastWorkingPoint(rate.getTime());
                log.trace("Set last working point: type=1.6, time={}", rate.getTime());
                context.setCurrentHigh(rate.getHigh());
            } else if (DOWN == context.getCurrentDirection() && rate.getClose().compareTo(rate.getOpen()) > 0 &&
                    context.getCurrentLow().compareTo(rate.getLow()) > 0) {
                setLastWorkingPrice(rate);
                context.setLastWorkingPoint(rate.getTime());
                log.trace("Set last working point: type=1.7, time={}", rate.getTime());
                context.setCurrentLow(rate.getLow());
            }

            context.setCurrentHigh(max(rate.getHigh(), context.getCurrentHigh()));
            context.setCurrentLow(max(rate.getLow(), context.getCurrentLow()));
        }
        // Обновили глобальный максимум
        else if (!alreadyReverse && context.getGlobalHigh() != null && Double.compare(rate.getHigh(), context.getGlobalHigh()) > 0 &&
                DOWN == context.getCurrentDirection() && !(context.getCurrentLow().compareTo(rate.getLow()) > 0)) {
            log.trace("Update globalHigh");
            result = reverse(rate.getTime(), UP, "Update global MAX", inMemoryRateRepository);
            context.setLastWorkingPoint(rate.getTime());
            log.trace("Set last working point: type=2.1, time={}", rate.getTime());
            alreadyReverse = true;
            context.setCurrentHigh(rate.getHigh());
        }
        // Обновили глобальный минимум
        else if (!alreadyReverse && context.getGlobalLow() != null && Double.compare(rate.getLow(), context.getGlobalLow()) < 0 &&
                UP == context.getCurrentDirection() && !(context.getCurrentHigh().compareTo(rate.getHigh()) < 0)) {
            log.trace("Update globalLow");
            result = reverse(rate.getTime(), DOWN, "Update global MIN", inMemoryRateRepository);
            context.setLastWorkingPoint(rate.getTime());
            log.trace("Set last working point: type=3.1, time={}", rate.getTime());
            alreadyReverse = true;
            context.setCurrentLow(rate.getLow());
        }
        // Обновили только локальный максимум
        else if (!alreadyReverse && Double.compare(rate.getHigh(), context.getLocalHigh()) > 0 &&
                !(Double.compare(rate.getLow(), context.getLocalLow()) < 0)) {
            log.trace("Update LocalHigh: currentDirection={}", context.getCurrentDirection());

            if (UP == context.getCurrentDirection()) {
                if (Double.compare(rate.getHigh(), context.getCurrentHigh()) > 0) {
                    context.setWaitingReverseCount(0);
                    setLastWorkingPrice(rate);
                    context.setLastWorkingPoint(rate.getTime());
                    context.setCurrentHigh(rate.getHigh());
                    log.trace("Set last working point: type=4.1, time={}, newCurrentHigh={}", rate.getTime(), context.getCurrentHigh());
                }
            } else if (DOWN == context.getCurrentDirection()) {
                log.trace("incWaitingReverseCount");
                context.incWaitingReverseCount();
                correctionWorkingPrice(rate.getTime());
                if (context.getWaitingReverseCount() >= reverseBarsCount) {
                    result = reverse(rate.getTime(), UP, "waitingReverseCount UP", inMemoryRateRepository);
                    alreadyReverse = true;
                } else if (context.getLastWorkingPrice() != null && context.getLastDownSwing() != null &&
                        (context.getLastDownSwing() - context.getLastWorkingPrice()) * 0.5
                                <= rate.getHigh() - context.getLastWorkingPrice()
                        && context.getWaitingReverseCount() >= reverseBarsCount - 1) {
                    int movingSize = inMemoryRateRepository.getShift(context.getLastWorkingPoint()) -
                            inMemoryRateRepository.getShift(rate.getTime());
                    if (movingSize > 1 && swingsStorage.getCount(stock, timeframe) > 1) {
                        int swingsCount = swingsStorage.getCount(stock, timeframe);
                        Optional<SwingEntity> swing1 = swingsStorage.getSwing(stock, timeframe, swingsCount - 1);
                        Optional<SwingEntity> swing2 = swingsStorage.getSwing(stock, timeframe, swingsCount - 2);
                        if (swing1.isPresent() && swing2.isPresent()) {
                            double swingSize = abs(swing1.get().getPrice() - context.getLastWorkingPrice());
                            double diff = abs(rate.getLow() - swing2.get().getPrice());
                            if (swingSize > 0 && diff / swingSize < 0.2) {
                                result = reverse(rate.getTime(), UP, "fastMoving UP", inMemoryRateRepository);
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
        else if (!alreadyReverse && !(Double.compare(rate.getHigh(), context.getLocalHigh()) > 0) &&
                Double.compare(rate.getLow(), context.getLocalLow()) < 0) {
            log.trace("Update LocalLow: currentDirection={}", context.getCurrentDirection());

            if (DOWN == context.getCurrentDirection()) {
                if (Double.compare(rate.getLow(), context.getCurrentLow()) < 0) {
                    context.setWaitingReverseCount(0);
                    setLastWorkingPrice(rate);
                    context.setLastWorkingPoint(rate.getTime());
                    context.setCurrentLow(rate.getLow());
                    log.trace("Set last working point: type=5.1, time={}, newCurrentLow={}", rate.getTime(), context.getCurrentLow());
                }
            } else if (UP == context.getCurrentDirection()) {
                log.trace("incWaitingReverseCount");
                context.incWaitingReverseCount();
                correctionWorkingPrice(rate.getTime());
                if (context.getWaitingReverseCount() >= reverseBarsCount) {
                    result = reverse(rate.getTime(), DOWN, "waitingReverseCount DOWN", inMemoryRateRepository);
                    alreadyReverse = true;
                } else if (context.getLastWorkingPrice() != null && context.getLastUpSwing() != null &&
                        (context.getLastWorkingPrice() - context.getLastUpSwing()) * 0.5
                                <= context.getLastWorkingPrice() - rate.getLow()
                        && context.getWaitingReverseCount() >= reverseBarsCount - 1) {
                    int movingSize = inMemoryRateRepository.getShift(context.getLastWorkingPoint()) -
                            inMemoryRateRepository.getShift(rate.getTime());
                    if (movingSize > 1 && swingsStorage.getCount(stock, timeframe) > 1) {
                        int swingsCount = swingsStorage.getCount(stock, timeframe);
                        Optional<SwingEntity> swing1 = swingsStorage.getSwing(stock, timeframe, swingsCount - 1);
                        Optional<SwingEntity> swing2 = swingsStorage.getSwing(stock, timeframe, swingsCount - 2);
                        if (swing1.isPresent() && swing2.isPresent()) {
                            double swingSize = abs(swing1.get().getPrice() - context.getLastWorkingPrice());
                            double diff = abs(rate.getLow() - swing2.get().getPrice());
                            if (swingSize > 0 && diff / swingSize < 0.2) {
                                result = reverse(rate.getTime(), DOWN, "fastMoving DOWN", inMemoryRateRepository);
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
                && Double.compare(rate.getHigh(), context.getLocalHigh()) > 0) {
            Optional<RateEntity> prevRate = inMemoryRateRepository.getRate(reverseBarsCount);
            if (prevRate.isPresent()
                    && inMemoryRateRepository.getHighestRate(prevRate.get().getTime(), rate.getTime()).filter(r -> r.getTime().equals(rate.getTime())).isPresent()
                    && inMemoryRateRepository.getLowestRate(prevRate.get().getTime(), rate.getTime()).filter(r -> r.getTime().equals(rate.getTime())).isEmpty()) {
                result = reverse(rate.getTime(), UP, "barsUpCount", inMemoryRateRepository);
                alreadyReverse = true;
            }
        }

        if (!alreadyReverse && UP == context.getCurrentDirection()
                && Double.compare(rate.getLow(), context.getLocalLow()) < 0) {
            Optional<RateEntity> prevRate = inMemoryRateRepository.getRate(reverseBarsCount);
            if (prevRate.isPresent()
                    && inMemoryRateRepository.getLowestRate(prevRate.get().getTime(), rate.getTime()).filter(r -> r.getTime().equals(rate.getTime())).isPresent()
                    && inMemoryRateRepository.getHighestRate(prevRate.get().getTime(), rate.getTime()).filter(r -> r.getTime().equals(rate.getTime())).isEmpty()) {
                result = reverse(rate.getTime(), DOWN, "barsDownCount", inMemoryRateRepository);
                alreadyReverse = true;
            }
        }

        context.setLocalHigh(rate.getHigh());
        context.setLocalLow(rate.getLow());

        if (!alreadyReverse
                && context.getCurrentDirection() != null
                && inMemoryRateRepository.getLatest(reverseBarsCount + 10).size() >= reverseBarsCount + 1) {
            LocalDateTime reverseBarTime = inMemoryRateRepository.getLatestRate().get().getTime();
            if (UP == context.getCurrentDirection() &&
                    (context.getLastWorkingPoint() != null && !context.getLastWorkingPoint().isAfter(reverseBarTime))) {
                Optional<LocalDateTime> newLastWorkingPoint = checkDownReverseByLastBars();
                if (newLastWorkingPoint.isPresent() && newLastWorkingPoint.get().isAfter(reverseBarTime)) {
                    result = reverse(reverseBarTime, DOWN, "downByLastBars", inMemoryRateRepository, newLastWorkingPoint.get());
                    //log.error("result={}", result);
                    alreadyReverse = true;
                }
            }
            if (DOWN == context.getCurrentDirection() &&
                    (context.getLastWorkingPoint() != null && !context.getLastWorkingPoint().isAfter(reverseBarTime))) {
                Optional<LocalDateTime> newLastWorkingPoint = checkUpReverseByLastBars();
                if (newLastWorkingPoint.isPresent() && newLastWorkingPoint.get().isAfter(reverseBarTime)) {
                    result = reverse(reverseBarTime, UP, "upByLastBars", inMemoryRateRepository, newLastWorkingPoint.get());
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
            Optional<LocalDateTime> workingPoint = lastBarsGrowDown(inMemoryRateRepository);
            if (workingPoint.isPresent()) {
                result = reverse(rate.getTime(), DOWN, "lastBarsGrowDown", inMemoryRateRepository);
                alreadyReverse = true;
            }
        } else {
            Optional<LocalDateTime> workingPoint = lastBarsGrowUp(inMemoryRateRepository);
            if (workingPoint.isPresent()) {
                result = reverse(rate.getTime(), UP, "lastBarsGrowUp", inMemoryRateRepository);
                alreadyReverse = true;
            }
        }

        if (!alreadyReverse) {
            List<RateEntity> latestRates = inMemoryRateRepository.getLatest(reverseBarsCount + 2);
            if (latestRates.size() == reverseBarsCount + 2) {
                boolean isReverse = true;
                RateEntity last = latestRates.get(latestRates.size() - 1);
                if (latestRates.size() > reverseBarsCount) {
                    for (int i = 0; i < latestRates.size() - 1; i++) {
                        RateEntity checkedRate = latestRates.get(i);
                        if (last.getHigh() - last.getLow() < (checkedRate.getHigh() - checkedRate.getLow()) * 2) {
                            isReverse = false;
                            break;
                        }
                        if (UP == context.getCurrentDirection() && last.getLow() >= checkedRate.getLow()) {
                            isReverse = false;
                            break;
                        } else if (DOWN == context.getCurrentDirection() && last.getHigh() <= checkedRate.getHigh()) {
                            isReverse = false;
                            break;
                        }
                    }
                    if (isReverse) {
                        boolean newDirection = context.getCurrentDirection() == UP ? DOWN : UP;

                        context.setLastWorkingPrice(newDirection == UP ? rate.getLow() : rate.getHigh());
                        context.setLastWorkingPoint(rate.getTime());

                        //result = reverse(rate.getTime(), newDirection, "runningBar" + (newDirection == UP ? "Up" : "Down"), inMemoryRateRepository);
                        //alreadyReverse = true;
                    }
                }
            }
        }

        if (debug) System.out.println("result: " + result);

        return result;
    }

    private void correctionWorkingPrice(LocalDateTime lastRateTime) {
        List<SwingEntity> latest = swingsStorage.getLatest(stock, timeframe, 1);
        if (latest.isEmpty()) {
            return;
        }
        if (latest.get(0).getDirection() == UP) {
            Optional<RateEntity> re = inMemoryRateRepository.getHighestRate(latest.get(0).getTime().plusMinutes(10), lastRateTime);
            if (re.isPresent()) {
                log.debug("correctionLocalHigh: newHigh={}", re.get().getHigh());
                context.setLocalHigh(re.get().getHigh());
                context.setLastWorkingPrice(re.get().getHigh());
                context.setLastWorkingPoint(re.get().getTime());
            }
        } else {
            Optional<RateEntity> re = inMemoryRateRepository.getLowestRate(latest.get(0).getTime().plusMinutes(10), lastRateTime);
            if (re.isPresent()) {
                log.debug("correctionLocalLow: newLow={}", re.get().getLow());
                context.setLocalLow(re.get().getLow());
                context.setLastWorkingPrice(re.get().getLow());
                context.setLastWorkingPoint(re.get().getTime());
            }
        }
    }

    private void correctNewLastWorkingPoint(boolean direction,
                                            RateEntity rate) {
        if (context.getLastWorkingPoint() == null) {
            return;
        }
        Optional<RateEntity> extremumRate = direction == UP
                ? inMemoryRateRepository.getLowestRate(context.getLastWorkingPoint(), rate.getTime())
                : inMemoryRateRepository.getHighestRate(context.getLastWorkingPoint(), rate.getTime());
        log.debug("correctNewLastWorkingPoint(): extremumRate={}, rate={}", extremumRate, rate);
        extremumRate.ifPresent(rt -> {
            context.setLastWorkingPoint(rt.getTime());
            if (direction == UP) {
                context.setLastWorkingPrice(rt.getLow());
            } else {
                context.setLastWorkingPrice(rt.getHigh());
            }
        });
    }

    Optional<LocalDateTime> checkDownReverseByLastBars() {
        if (inMemoryRateRepository.getLatest(reverseBarsCount + 1).size() < reverseBarsCount + 1) {
            return Optional.empty();
        }

        RateEntity checkedRate = inMemoryRateRepository.getRate(reverseBarsCount)
                .orElseThrow();
        double high = checkedRate.getHigh();
        double low = checkedRate.getLow();
        double lowest = low;
        LocalDateTime time = checkedRate.getTime();

        for (int i = 0; i < reverseBarsCount; i++) {
            RateEntity curRate = inMemoryRateRepository.getRate(i)
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
        if (inMemoryRateRepository.getLatest(reverseBarsCount + 1).size() < reverseBarsCount + 1) {
            return Optional.empty();
        }

        RateEntity checkedRate = inMemoryRateRepository.getRate(reverseBarsCount)
                .orElseThrow();
        double high = checkedRate.getHigh();
        double low = checkedRate.getLow();
        double highest = high;
        LocalDateTime time = checkedRate.getTime();

        for (int i = 0; i < reverseBarsCount; i++) {
            RateEntity curRate = inMemoryRateRepository.getRate(i)
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

    private void setLastWorkingPrice(RateEntity rate) {
        if (context.getLastWorkingPrice() == null) {
            double price = context.getCurrentDirection() == UP ? rate.getHigh() : rate.getLow();
            context.setLastWorkingPrice(price);
            log.debug("Set last working price: {}", price);
        } else if (context.getCurrentDirection() == UP && Double.compare(rate.getHigh(), context.getLastWorkingPrice()) > 0) {
            context.setLastWorkingPrice(rate.getHigh());
            log.debug("Set last working price: {}", rate.getHigh());
        } else if (context.getCurrentDirection() == DOWN && Double.compare(rate.getLow(), context.getLastWorkingPrice()) < 0) {
            context.setLastWorkingPrice(rate.getLow());
            log.debug("Set last working price: {}", rate.getLow());
        }
    }

    private Optional<LocalDateTime> lastBarsGrowUp(InMemoryRateRepository inMemoryRateRepository) {
        int barsCount = inMemoryRateRepository.getLatest(reverseBarsCount * 2 + 11).size();
        if (barsCount <= reverseBarsCount * 2 + 10) return Optional.empty();
        RateEntity bar = inMemoryRateRepository.getRate(0)
                .orElseThrow();
        double high = bar.getHigh();
        double low = bar.getLow();
        double min = bar.getLow();
        LocalDateTime workingPoint = bar.getTime();

        for (int i = 1; i <= reverseBarsCount; i++) {
            RateEntity prevBar = inMemoryRateRepository.getRate(i)
                    .orElseThrow();
            if (bar.getHigh() < prevBar.getHigh()) return Optional.empty();
            if (min > prevBar.getLow()) {
                min = prevBar.getLow();
                workingPoint = prevBar.getTime();
            }
            bar = prevBar;
        }
        if (bar.getLow() >= low) return Optional.empty();
        for (int i = reverseBarsCount + 1; i < reverseBarsCount * 2 && i < barsCount - 1; i++) {
            RateEntity prevBar = inMemoryRateRepository.getRate(i)
                    .orElseThrow();
            if (prevBar.getHigh() >= high) return Optional.empty();
        }
        return Optional.of(workingPoint);
    }

    private Optional<LocalDateTime> lastBarsGrowDown(InMemoryRateRepository inMemoryRateRepository) {
        int barsCount = inMemoryRateRepository.getLatest(reverseBarsCount * 2 + 11).size();
        if (barsCount <= reverseBarsCount * 2 + 10) return Optional.empty();
        RateEntity bar = inMemoryRateRepository.getRate(0)
                .orElseThrow();
        double low = bar.getLow();
        double high = bar.getHigh();
        double max = bar.getHigh();
        LocalDateTime workingPoint = bar.getTime();

        for (int i = 1; i <= reverseBarsCount; i++) {
            RateEntity prevBar = inMemoryRateRepository.getRate(i)
                    .orElseThrow();
            if (bar.getLow() > prevBar.getLow()) return Optional.empty();
            if (max < prevBar.getHigh()) {
                max = prevBar.getHigh();
                workingPoint = prevBar.getTime();
            }
            bar = prevBar;
        }
        if (bar.getHigh() <= high) return Optional.empty();
        for (int i = reverseBarsCount + 1; i < reverseBarsCount * 2 && i < barsCount - 1; i++) {
            RateEntity prevBar = inMemoryRateRepository.getRate(i)
                    .orElseThrow();
            if (prevBar.getLow() <= low) return Optional.empty();
        }
        return Optional.of(workingPoint);
    }

    private Optional<SwingPoint> reverse(LocalDateTime time,
                                         boolean direction,
                                         String cause,
                                         InMemoryRateRepository inMemoryRateRepository) {
        return reverse(time, direction, cause, inMemoryRateRepository, null);
    }

    private Optional<SwingPoint> reverse(LocalDateTime time,
                                         boolean direction,
                                         String cause,
                                         InMemoryRateRepository inMemoryRateRepository,
                                         LocalDateTime newLastWorkingPoint) {
        log.debug("Reverse: direction={}, time={}, cause={}, lastWorkingPoint={}, period={}, newLastWorkingPoint={}",
                direction, time, cause, context.getLastWorkingPoint(), timeframe, newLastWorkingPoint);
        Objects.requireNonNull(time, "time is null");

        LocalDateTime prevLastWorkingPoint = context.getLastWorkingPoint();
        if (context.getLastWorkingPoint() == null) {
            //throw new RuntimeException("LastWorkingPoint is null: time=" + time + ", context=" + context);
            context.setLastWorkingPoint(time);
            //return Optional.empty();
        }

        int shift = inMemoryRateRepository.getShift(context.getLastWorkingPoint());
        if (shift == -1) {
            log.warn("shift=-1: time={}, context.getTimeframe()={}, context.getLastWorkingPoint()={}", time,
                    timeframe, context.getLastWorkingPoint());
        }
        RateEntity rate = inMemoryRateRepository.getRate(shift)
                .orElseThrow();
        double price = direction == UP ? rate.getLow() : rate.getHigh();
        if (context.getLastWorkingPrice() != null) {
            price = context.getLastWorkingPrice();
        }
        log.debug("Reverse: price={}, time={}, shift={}, reason={}, reverseTime={}", price, context.getLastWorkingPoint(), shift,
                cause, inMemoryRateRepository.getRate(0).map(RateEntity::getTime).orElse(null));
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
            int nextShift = inMemoryRateRepository.getShift(time);
            if (nextShift == shift) {
                log.debug("Set last working point by shift: {}", time);
                context.setLastWorkingPoint(time);
            }
            //debug(time, "nextShift=" + IntegerToString(nextShift) + "; shift=" + IntegerToString(shift));
            //debug(time, "nextShift=" + TimeToString(iTime(symbol, period, nextShift)) + "; shift=" + TimeToString(iTime(symbol, period, shift)));
            if (direction == UP) {
                inMemoryRateRepository.getHighestRate(prevLastWorkingPoint, time)
                        .ifPresent(rt -> {
                            context.setLastWorkingPoint(rt.getTime());
                            context.setLastWorkingPrice(rt.getHigh());
                        });
                //lastWorkingPoint = iTime(symbol, period, iHighest(symbol, TFMigrate(period), MODE_HIGH, shift - nextShift, nextShift));
                //debug(time, "1: " + IntegerToString(iHighest(symbol, TFMigrate(period), MODE_HIGH, shift - nextShift, nextShift)));
                //debug(time, "1: " + TimeToString(iTime(symbol, period, 50)));
            } else {
                inMemoryRateRepository.getLowestRate(prevLastWorkingPoint, time)
                        .ifPresent(rt -> {
                            context.setLastWorkingPoint(rt.getTime());
                            context.setLastWorkingPrice(rt.getLow());
                        });
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
