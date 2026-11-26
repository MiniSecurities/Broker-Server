package com.sangyunpark99.brokerserver.quote;

import com.sangyunpark99.brokerserver.quote.service.QuoteStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteGenerator {

    private static final double RATE_PER_SECONDS = 10;
    private static final BigDecimal TICK = new BigDecimal("100");

    private final QuoteStreamService quoteStreamService;
    private final ScheduledExecutorService scheduledExecutor;

    private final Map<String, AtomicReference<BigDecimal>> prices = new ConcurrentHashMap<>(
            Map.of("035720", new AtomicReference<>(new BigDecimal("100000")))
    );

    private final AtomicLong sequence = new AtomicLong();

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        prices.keySet().forEach(this::scheduledNext);
    }

    private void scheduledNext(String symbol) {
        long delayMillis = (long) (-Math.log(1 - ThreadLocalRandom.current().nextDouble()) / RATE_PER_SECONDS * 1000);
        scheduledExecutor.schedule(() -> publish(symbol), delayMillis, TimeUnit.MILLISECONDS);
    }

    public void publish(String symbol) {
        try {

            AtomicReference<BigDecimal> price = prices.get(symbol);
            BigDecimal current = price.get();
            BigDecimal next = current.add(TICK.multiply(BigDecimal.valueOf(pickTicks())));
            if (next.compareTo(TICK) < 0) {
                next = TICK;
            }
            price.set(next);

            Quote quote = new Quote(sequence.incrementAndGet(), symbol, next, LocalDateTime.now());
            quoteStreamService.broadcast(quote);
        } catch (Exception e) {
            log.error("[error] {}", e.getMessage());
        }
        finally {
            scheduledNext(symbol);
        }
    }

    private int pickTicks() {
        double r = ThreadLocalRandom.current().nextDouble();
        int size;
        if (r < 0.4) {
            size = 0;
        } else if (r < 0.8) {
            size = 1;
        } else if (r < 0.95) {
            size = 2;
        } else {
            size = 3;
        }
        return ThreadLocalRandom.current().nextBoolean() ? size : -size;
    }
}
