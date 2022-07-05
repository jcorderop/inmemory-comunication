package org.jcp.bus;

import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;


@Slf4j
public class EventBusPerformance {
    //https://kelvinleong.github.io/java/2020/01/09/vertx-thread-block.html

    public static void main(String[] args) {
        final Function<Long, Object> messageToSend = id -> id;
        EventBusTools.executePerformanceTest(EventBusListener.class.getName(), messageToSend);
    }

    public static class EventBusListener extends AbstractVerticle {
        @Override
        public void start(final Promise<Void> startPromise) {
            startPromise.complete();
            log.info("Working on " + Thread.currentThread().getName());
            Function<Message, Object> deserializationFunc = message -> message.body();
            vertx.executeBlocking(EventBusTools.createBusConsumer(vertx, deserializationFunc));
        }
    }

    
}
