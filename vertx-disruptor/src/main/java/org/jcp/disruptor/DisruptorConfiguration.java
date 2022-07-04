package org.jcp.disruptor;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.extern.slf4j.Slf4j;
import org.jcp.disruptor.model.OrderEvent;
import org.jcp.disruptor.model.OrderEventFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
public class DisruptorConfiguration {
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	@Bean
	public Disruptor<OrderEvent> disruptor() {
		log.info("Preparing Disruptor...");
		final Executor executor = Executors.newCachedThreadPool();
		final OrderEventFactory factory = new OrderEventFactory();
		final int bufferSize = 1024;
		Disruptor<OrderEvent> disruptor = new Disruptor<OrderEvent>(
				factory, bufferSize, executor,
				ProducerType.MULTI,
				new BlockingWaitStrategy()
//				new SleepingWaitStrategy()
			);
		disruptor.handleEventsWithWorkerPool(
				//new DisruptorConsumer(),
				//new DisruptorConsumer(),
				//new DisruptorConsumer(),
				new DisruptorConsumer());
		return disruptor;
	}
	
	@Bean
	public DisruptorProducer producer(final Disruptor<OrderEvent> disruptor) {
		log.info("Preparing DisruptorProducer...");
		final RingBuffer<OrderEvent> ringBuffer = disruptor.getRingBuffer();
		final DisruptorProducer producer =  new DisruptorProducer();
		producer.setRingBuffer(ringBuffer);
		return producer;
	}
	
	@Bean
	public SquareDisruptor squareDisruptor(final Disruptor<OrderEvent> disruptor) {
		log.info("Preparing SquareDisruptor...");
		final SquareDisruptor squareDisruptor = new SquareDisruptor();
		squareDisruptor.setDisruptor(disruptor);
		return squareDisruptor;
	}

}
