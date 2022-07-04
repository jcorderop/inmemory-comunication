package org.jcp.api;

import lombok.extern.slf4j.Slf4j;
import org.jcp.disruptor.DisruptorProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
//@AllArgsConstructor
@Service
public class OrderService {

    @Autowired
    private DisruptorProducer disruptorProducer;

    public OrderService(DisruptorProducer disruptorProducer) {
        this.disruptorProducer = disruptorProducer;
    }

    public void processNewOrder(final long orderId) {
        //log.info("New order to be processed... {}", orderId);
        disruptorProducer.publishData(orderId);
    }

}
