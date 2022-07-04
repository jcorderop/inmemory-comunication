package org.jcp.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "springBootApp.workOffline=true")
class DisruptorSpringBootTest {

    private static final Long ORDERS = 10000000L;
    @Autowired
    OrderService orderService;

    @Test
    void processNewOrder() {
        for (int i = 1; i <= ORDERS; i++) {
            Long orderId = 1000000000L+i;
            orderService.processNewOrder(orderId);
        };

    }

}