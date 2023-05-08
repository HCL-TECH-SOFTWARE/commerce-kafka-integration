package com.hcl.commerce.kafka.consumer;

import com.ibm.commerce.command.ControllerCommand;

public interface KafkaConsumerCmd extends ControllerCommand{
	static final String defaultCommandClassName = "com.hcl.commerce.kafka.consumer.KafkaConsumerCmdImpl";
}
