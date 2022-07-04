package org.jcp.disruptor;

import com.lmax.disruptor.RingBuffer;
import org.jcp.disruptor.model.OrderEvent;

public class DisruptorProducer {
	
	private RingBuffer<OrderEvent> ringBuffer;
	
	public void setRingBuffer(final RingBuffer<OrderEvent> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

	public void publishData(final long value) {
		
		final long sequence = ringBuffer.next();
		try {
			final OrderEvent event = ringBuffer.get(sequence);
			event.setOrderId(value);
		} finally {
			ringBuffer.publish(sequence);
		}
	}

}
