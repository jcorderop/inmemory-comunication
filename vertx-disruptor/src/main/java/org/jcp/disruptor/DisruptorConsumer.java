package org.jcp.disruptor;

import com.lmax.disruptor.WorkHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jcp.disruptor.model.OrderEvent;

import java.util.Optional;

@Slf4j
@Setter
@Getter
public class DisruptorConsumer implements WorkHandler<OrderEvent> {

	private int counter = 1000000;
	private Optional<Long> latency = Optional.empty();

	@Override
	public void onEvent(final OrderEvent event) {
		if (!latency.isPresent())
			latency = Optional.of(System.currentTimeMillis());
		//log.info("Consumer [" + Thread.currentThread().getName() + "] " + event.getOrderId());
		if (event.getOrderId() % counter == 0) {
			log.info("Counter: {} - Latency: {}", counter, (System.currentTimeMillis() - latency.get()));
			latency = Optional.of(System.currentTimeMillis());
		}
	}

}
