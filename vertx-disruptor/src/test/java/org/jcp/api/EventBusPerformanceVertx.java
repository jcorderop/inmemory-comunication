package org.jcp.api;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.extern.slf4j.Slf4j;
import org.jcp.bus.EventBusPerformance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(VertxExtension.class)
public class EventBusPerformanceVertx {

    @Test
    void test_orders(Vertx vertx, VertxTestContext testContext){
        EventBusPerformance.executePerformanceTest();
        testContext.completeNow();
    }
}
