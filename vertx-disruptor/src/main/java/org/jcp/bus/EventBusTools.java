package org.jcp.bus;

import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;
import io.vertx.ext.dropwizard.MetricsService;
import lombok.extern.slf4j.Slf4j;
import org.jcp.bus.model.Order;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

@Slf4j
public class EventBusTools {
    //https://kelvinleong.github.io/java/2020/01/09/vertx-thread-block.html

    public static final String TOPIC_ORDER_NEW = "test.queue";

    public static final long ORDER_ID_INIT = 1_000_000_000L;
    public static final long ORDERS_TO_CREATE = 1_000_000_000L;
    public static final int snapshotCounter = 5_000_000;
    public static final int shift = 1;

    public static long publishFinished = 0;

    public static long initLatency = 0;

    public static final boolean isMonitoringActive = false;

    public static Vertx getVertx(boolean monitoringActive) {
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

    public static void getBusMetrics(Vertx vertx) {
        //-Dvertx.metrics.options.enabled=true
        MetricsService metricsService = MetricsService.create(vertx);

        JsonObject metricsBus = metricsService.getMetricsSnapshot(vertx.eventBus());
        //metrics.getJsonObject("handlers");
        System.out.println(metricsBus);

        JsonObject metricsVertx = metricsService.getMetricsSnapshot(vertx);
        System.out.println(metricsVertx);
    }

    private static Vertx preparingEnvironment(String consumerClassName) {
        final AtomicBoolean ready = new AtomicBoolean(false);
        var vertx = EventBusTools.getVertx(EventBusTools.isMonitoringActive);
        log.info("Metrics enabled Vertx.vertx(): "+Vertx.vertx().isMetricsEnabled());
        log.info("Metrics enabled vertx: "+ vertx.isMetricsEnabled());
        vertx.eventBus().registerDefaultCodec(Order.class, new EventBusMessageCodec<>(Order.class));
        vertx.deployVerticle(consumerClassName).onSuccess(s -> ready.set(true));
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
        EventBusTools.initLatency = System.currentTimeMillis();
        return vertx;
    }

    public static void executePerformanceTest(String consumerClassName, Function<Long, Object> messageToSend) {
        log.info("Starting project...");
        final var vertx = EventBusTools.preparingEnvironment(consumerClassName);
        EventBusTools.publishEvents(vertx, messageToSend);
        log.info("Has been Finished...");
    }

    private static void publishEvents(Vertx vertx, Function<Long, Object> messageToSend) {
        log.info("Publishing executeBlocking on " + Thread.currentThread().getName());
        final long publishThrottleInms = 10;
        long next = System.nanoTime()+publishThrottleInms;
        for (int i = 0; i <= EventBusTools.ORDERS_TO_CREATE+EventBusTools.shift; i++) {
            vertx.eventBus().send(EventBusTools.TOPIC_ORDER_NEW,
                    messageToSend.apply(EventBusTools.ORDER_ID_INIT+i),
                    new DeliveryOptions().setLocalOnly(false));
            while (next >= System.nanoTime()) {
            }
            next = System.nanoTime()+publishThrottleInms;
        }
        log.info(">>>> Published Done...");
        EventBusTools.publishFinished = System.currentTimeMillis();
    }

    public static Handler<Promise<Object>> createBusConsumer(final Vertx vertx,
                                                             final Function<Message, Object> deserializationFunc) {
        return promise -> {
            log.info("Working executeBlocking on " + Thread.currentThread().getName());
            final AtomicLong startLatency = new AtomicLong(-1);
            final AtomicLong incomingOrderId = new AtomicLong(0);
            vertx.eventBus().consumer(EventBusTools.TOPIC_ORDER_NEW, message -> {
                deserializationFunc.apply(message);
                final long event = incomingOrderId.addAndGet(1);
                if (startLatency.get() == -1)
                    startLatency.set(System.nanoTime());
                if (event % EventBusTools.snapshotCounter == 0) {
                    System.out.println(Thread.currentThread().getName() +
                                ", Last event Id " + event +
                                ", N. events: "+EventBusTools.snapshotCounter +
                                ", Latency ms: " + (System.nanoTime() - startLatency.get())/1000000);
                    startLatency.set(System.nanoTime());
                    if (EventBusTools.isMonitoringActive) {
                        EventBusTools.getBusMetrics(vertx);
                    }
                }
                if (event > EventBusTools.ORDERS_TO_CREATE+EventBusTools.shift) {
                    log.info(">>>> All done....");
                    final long lastLatency = System.currentTimeMillis();
                    log.info(">>>> Latency to process: "+EventBusTools.ORDERS_TO_CREATE+" ms: "+ (lastLatency - EventBusTools.initLatency));
                    log.info(">>>> Latency enqueuing ms: "+ (lastLatency - EventBusTools.publishFinished));
                }
            });
        };
    }
}
