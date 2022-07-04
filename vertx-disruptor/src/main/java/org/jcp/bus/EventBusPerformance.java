package org.jcp.bus;

import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;
import io.vertx.ext.dropwizard.MetricsService;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class EventBusPerformance {
    //https://kelvinleong.github.io/java/2020/01/09/vertx-thread-block.html

    private static final String TOPIC_ORDER_NEW = "test.queue";

    private static final long ORDER_ID_INIT = 1000000000L;
    private static final long ORDERS_TO_CREATE = 100000000L;
    private static final int snapshotCounter = 1000000;
    private static final int shift = 1;

    private static long publishFinished = 0;

    private static long initLatency = 0;

    private static final boolean isMonitoringActive = false;

    public static void main(String[] args) {
        executePerformanceTest();
    }

    public static void executePerformanceTest() {
        final AtomicBoolean ready = new AtomicBoolean(false);
        log.info("Starting project...");
        var vertx = getVertx(isMonitoringActive);
        log.info("Metrics enabled Vertx.vertx(): "+Vertx.vertx().isMetricsEnabled());
        log.info("Metrics enabled vertx: "+ vertx.isMetricsEnabled());
        vertx.deployVerticle(new EventBusListener()).onSuccess(s -> ready.set(true));
        while(true) {
            if (ready.get()) {
                break;
            }
            try {
                log.info("Waiting...");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        log.info("Ready to publish");
        initLatency = System.currentTimeMillis();
        publishEvents(vertx);
        log.info("Has been Finished...");
    }

    private static Vertx getVertx(boolean monitoringActive) {
        if (monitoringActive) {
            return Vertx.vertx(new VertxOptions().setMetricsOptions(
                    new DropwizardMetricsOptions()
                            .setEnabled(true)
                            //.addMonitoredEventBusHandler(new Match().setValue(TOPIC_ORDER_NEW))
                            .addMonitoredEventBusHandler(new Match().setValue(".?").setType(MatchType.REGEX))));
        } else {
           return Vertx.vertx();
        }
    }

    private static void getBusMetrics(Vertx vertx) {
        MetricsService metricsService = MetricsService.create(vertx);

        JsonObject metricsBus = metricsService.getMetricsSnapshot(vertx.eventBus());
        //metrics.getJsonObject("handlers");
        System.out.println(metricsBus);

        JsonObject metricsVertx = metricsService.getMetricsSnapshot(vertx);
        System.out.println(metricsVertx);
    }

    private static void publishEvents(Vertx vertx) {
        log.info("Publishing executeBlocking on " + Thread.currentThread().getName());
        final long publishThrottleInms = 50;
        long next = System.nanoTime()+publishThrottleInms;
        for (int i = 0; i <= ORDERS_TO_CREATE+shift; i++) {
            final Long orderId = ORDER_ID_INIT+i;
            vertx.eventBus().send(TOPIC_ORDER_NEW, orderId, new DeliveryOptions().setLocalOnly(true));
            while (next >= System.nanoTime()) {
            }
            next = System.nanoTime()+publishThrottleInms;
        }
        log.info(">>>> Published Done...");
        publishFinished = System.currentTimeMillis();
    }

    public static class EventBusListener extends AbstractVerticle {
        private Optional<Long> latency = Optional.empty();

        @Override
        public void start(final Promise<Void> startPromise) {
            startPromise.complete();
            log.info("Working on " + Thread.currentThread().getName());
            vertx.executeBlocking(event -> {
                log.info("Working executeBlocking on " + Thread.currentThread().getName());
                final AtomicLong atomicLong = new AtomicLong(0);
                vertx.eventBus().<Long>consumer(TOPIC_ORDER_NEW, message -> {
                    final Long orderId = atomicLong.addAndGet(1);
                    if (!latency.isPresent())
                        latency = Optional.of(System.nanoTime());
                    if (orderId % snapshotCounter == 0) {
                        System.out.println("Received message on " + Thread.currentThread().getName());
                        System.out.println("events: "+snapshotCounter+" - Latency ms: "+ (System.nanoTime() - latency.get())/1000000);
                        System.out.println("Last Order Id " + orderId);
                        latency = Optional.of(System.nanoTime());
                        if (isMonitoringActive) {
                            getBusMetrics(vertx);
                        }
                    }
                    if (orderId > ORDERS_TO_CREATE+shift) {
                        log.info(">>>> All done....");
                        final long lastLatency = System.currentTimeMillis();
                        log.info(">>>> Latency to process: "+ORDERS_TO_CREATE+" ms: "+ (lastLatency - initLatency));
                        log.info(">>>> Latency enqueuing ms: "+ (lastLatency - publishFinished));
                    }
                });
            });
        }
    }
}
