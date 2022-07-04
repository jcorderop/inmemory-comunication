package org.jcp.api;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.extern.slf4j.Slf4j;
import org.jcp.vertx.VertxComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(VertxExtension.class)
class RestApiVerticleTest extends AbstractRestApiTest {

    private final static int WAIT_TYPE_SEC = 5;
    public static final String TOPIC_ORDER_NEW = "order.new";

    @Autowired
    VertxComponent vertxComponent;

    @Test
    void test_orders(Vertx vertx, VertxTestContext testContext){
        Long ORDERS = 1000000L;
        AtomicLong counter = new AtomicLong(1);

        for (int i = 0; i < ORDERS; i++) {
            counter.addAndGet(1);
            Long orderId = 1000000000L+i;
            vertx.eventBus().publish(TOPIC_ORDER_NEW, orderId);
        };

        //when

        try {
            long last = 0;
            while(last < ORDERS) {
                log.info(">>>>>>>> Test is waiting... {}", last <= ORDERS);
                TimeUnit.SECONDS.sleep(1);
                last = counter.get();
                log.info(">>>>>>>> Counter: {}",last);
            }
            log.info(">>>>>>>> Waiting to finish...");
            testContext.awaitCompletion(1, TimeUnit.MINUTES);
            log.info(">>>>>>>> Completing test...");
            testContext.completeNow();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    static class WebClientListener extends AbstractVerticle {

        @Override
        public void start(final Promise<Void> startPromise) {
            final var webClient = createWebClient(vertx);
            startPromise.complete();

            vertx.eventBus().<Long>consumer(TOPIC_ORDER_NEW, message -> {
                Long orderId = message.body();
                log.info("Test New Order to send: {}", orderId);

                webClient.get("/order/"+orderId).send().onSuccess(event -> log.info("Sending...")).onComplete(event -> {
                    HttpResponse<Buffer> response = event.result();
                    final var json = response.bodyAsJsonObject();
                    log.info("Response: {}", json);
                    assertEquals(200, response.statusCode());

                    assertEquals(HttpHeaderValues.APPLICATION_JSON.toString(), response.getHeader(HttpHeaders.CONTENT_TYPE.toString()));
                    assertEquals("my-value", response.getHeader("my-header"));
                });
            });

        }
        private WebClient createWebClient(final Vertx vertx) {
            return WebClient.create(vertx, new WebClientOptions().setDefaultPort(RestApiVerticle.REST_API_PORT));
        }
    }
}