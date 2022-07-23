package org.jcp.api;

import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;
import org.jcp.disruptor.DisruptorConfiguration;
import org.jcp.disruptor.DisruptorProducer;
import org.jcp.disruptor.SquareDisruptor;
import org.jcp.disruptor.model.OrderEvent;
import org.junit.jupiter.api.Test;

@Slf4j
class DisruptorOnlyJavaTest {

    private static final Long ORDERS = 10000000L;

    @Test
    void processNewOrder() {
        DisruptorConfiguration disruptorConfiguration = new DisruptorConfiguration();
        Disruptor<OrderEvent> disruptor = disruptorConfiguration.disruptor();
        SquareDisruptor squareDisruptor = disruptorConfiguration.squareDisruptor(disruptor);
        squareDisruptor.startDisruptor();
        DisruptorProducer producer = disruptorConfiguration.producer(disruptor);
        OrderService orderService = new OrderService(producer);


        for (int i = 1; i <= ORDERS; i++) {
            long orderId = 1000000000L+i;
            //log.info("{}",orderId);
            orderService.processNewOrder(orderId);
        }

    }

}