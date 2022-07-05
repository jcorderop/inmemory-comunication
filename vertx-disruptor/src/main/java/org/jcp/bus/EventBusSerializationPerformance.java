package org.jcp.bus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import lombok.extern.slf4j.Slf4j;
import org.jcp.bus.model.Order;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;


@Slf4j
public class EventBusSerializationPerformance {
    //https://kelvinleong.github.io/java/2020/01/09/vertx-thread-block.html

    public static void main(String[] args) {

        final Function<Long, Object> messageToSend = id -> new Order(id,
                1000.0,
                1.25,
                2.58,
                "OK",
                "1234567890",
                "btcusd",
                "some comments...");
        EventBusTools.executePerformanceTest(EventBusSerializationListener.class.getName(), messageToSend);
    }

    public static class EventBusSerializationListener extends AbstractVerticle {
        @Override
        public void start(final Promise<Void> startPromise) {
            startPromise.complete();
            log.info("Working on " + Thread.currentThread().getName());
            Function<Message, Object> deserializationFunc = message -> {
                final Order order = (Order) message.body();
                long oid = order.orderId();
                //System.out.println(order);
                return order;
            };
            vertx.executeBlocking(EventBusTools.createBusConsumer(vertx, deserializationFunc));
        }
    }

    
}
