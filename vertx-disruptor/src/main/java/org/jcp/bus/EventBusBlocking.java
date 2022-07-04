package org.jcp.bus;

import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class EventBusBlocking {
    //https://www.redhat.com/en/blog/troubleshooting-performance-vertx-applications-part-ii-%E2%80%94-preventing-event-loop-delays
    private static final String TOPIC_ORDER_NEW = "test.queue";
    private static final Long ORDERS = 100000000L;

    private static AtomicLong orderId = new AtomicLong(1000000000L);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(() -> new EventConsumer(), new DeploymentOptions().setInstances(1));
        //vertx.deployVerticle(EventPublish.class.getName(), new DeploymentOptions().setInstances(1).setWorker(true));
        vertx.deployVerticle(() -> new EventPublish(), new DeploymentOptions().setInstances(2));
        System.out.println("Started...");
    }

    static class EventConsumer extends AbstractVerticle {

        private  AtomicLong atomicLong = new AtomicLong(0);
        private Optional<Long> latency = Optional.empty();

        @Override
        public void start(Promise<Void> startPromise) throws Exception {
            // work handler
            final Long counter = 1000000L;
            Handler<Message<Long>> handler = message -> {
                //System.out.println("Received message on " + Thread.currentThread().getName());
                //Long counter = atomicLong.addAndGet(1);
                Long orderId = message.body();
                // do work
                //System.out.println("Working ...");
                if (!latency.isPresent())
                    latency = Optional.of(System.nanoTime());
                //log.info("Consumer [" + Thread.currentThread().getName() + "] " + orderId);
                if (orderId % counter == 0) {
                    System.out.println("Received message on " + Thread.currentThread().getName());
                    System.out.println("Counter: "+counter+" - Latency: "+ (System.nanoTime() - latency.get())/1000000);
                    latency = Optional.of(System.nanoTime());
                }
                //message.reply("OK");
            };

            // wait for work
            vertx.eventBus().consumer(TOPIC_ORDER_NEW, handler).completionHandler(r -> {
                startPromise.complete();
            });
            System.out.println("Ready to read... on " + Thread.currentThread().getName());
        }
    }

    static class EventPublish extends AbstractVerticle {

        @Override
        public void start(Promise<Void> startPromise) {
            // reply handler
            startPromise.complete();

            // dispatch work
            vertx.executeBlocking(event -> {
                Handler<AsyncResult<Message<Long>>> replyHandler = message -> {
                    //System.out.println("Received reply '" + message.result().body() + "' on " + Thread.currentThread().getName());
                };
                vertx.setPeriodic(1, p -> {
                    for (int i = 0; i < 500; i++) {
                        vertx.eventBus().send(TOPIC_ORDER_NEW, orderId.addAndGet(1));//, replyHandler);
                    }

                });
            } );
            System.out.println("Ready to publish...  on " + Thread.currentThread().getName());

        }
    }
}
