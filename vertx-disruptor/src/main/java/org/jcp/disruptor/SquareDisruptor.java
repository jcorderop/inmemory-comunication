package org.jcp.disruptor;

import com.lmax.disruptor.dsl.Disruptor;
import org.jcp.disruptor.model.OrderEvent;

import javax.annotation.PostConstruct;

public class SquareDisruptor {
	
	private Disruptor<OrderEvent> disruptor;

	public void setDisruptor(final Disruptor<OrderEvent> disruptor) {
		this.disruptor = disruptor;
	}
	
	@PostConstruct
	public void startDisruptor() {
		disruptor.start();
	}

}
