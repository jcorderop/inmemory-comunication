package org.jcp.api;

import org.jcp.disruptor.DisruptorProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderConfiguration {
/*
    @Bean
    public OrderRouter orderRouter (final OrdersHandler ordersHandler) {
        return new OrderRouter(ordersHandler);
    }

    @Bean
    public OrderService orderService(final DisruptorProducer disruptorProducer) {
        return new OrderService(disruptorProducer);
    }

    @Bean
    public OrdersHandler ordersHandler (final OrderService orderService) {
        return new OrdersHandler(orderService);
    }

    @Bean
    public RestApiVerticle restApiVerticle(OrderRouter orderRouter) {
        return new RestApiVerticle(orderRouter);
    }*/
}
