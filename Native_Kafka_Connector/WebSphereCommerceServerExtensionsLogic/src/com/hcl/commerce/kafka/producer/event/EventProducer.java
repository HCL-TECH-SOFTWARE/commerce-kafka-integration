package com.hcl.commerce.kafka.producer.event;

import com.ibm.commerce.exception.ECApplicationException;

public interface EventProducer<T> {
	public void fireEvent(T event) throws ECApplicationException;
}
