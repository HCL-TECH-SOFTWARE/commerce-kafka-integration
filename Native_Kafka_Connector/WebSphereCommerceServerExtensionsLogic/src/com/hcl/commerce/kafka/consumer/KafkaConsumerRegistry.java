package com.hcl.commerce.kafka.consumer;
/**
*-----------------------------------------------------------------
 Copyright [2022] [HCL America, Inc.]

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

*-----------------------------------------------------------------
**/

import static com.hcl.commerce.kafka.constants.KafkaConstants.KAFKA_HOST;
import static com.hcl.commerce.kafka.constants.KafkaConstants.KAFKA_PORT;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import com.hcl.commerce.kafka.helper.KafkaHelper;
import com.ibm.commerce.foundation.logging.LoggingHelper;
import com.ibm.commerce.registry.Registry;

/**
 * This registry class is used to start the kafka consumer at server start.
 *
 */
public class KafkaConsumerRegistry implements Registry {
	private static final String CLASS_NAME = "KafkaConsumerRegistry";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	/**
	 * Initialize the kafka config properties that will be used by MessageConsumer
	 * class to connect with kafka server.
	 */
	private static void initSystemProp() {
		System.setProperty("wc.remote.kafka", "");
		System.setProperty("kafka.ssl.keystore.location", "");
		System.setProperty("kafka.ssl.keystore.password", "");
		System.setProperty("kafka.ssl.truststore.location", "");
		System.setProperty("kafka.ssl.truststore.password", "");
		System.setProperty("kafka.ssl.keystore.type", "");
		System.setProperty("kafka.ssl.truststore.type", "");
		System.setProperty("kafka.sasl.jaas.config.username", "");
		System.setProperty("kafka.sasl.jaas.config.password", "");
		System.setProperty("kafka.topic.partition", "0");
		System.setProperty("kafka.topic.replication", "0");
		System.setProperty("request.timeout.ms", "0");

	}

	@Override
	public void initialize() throws Exception {
		final String methodName = "initialize";
		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.entering(CLASS_NAME, methodName);
		
		String topic = KafkaHelper.getValueByConfigGroupingNameAndPropertyName("kafkaConfig",
				"com.hcl.kafka.consumer.inventory.topic");
		LOGGER.log(Level.INFO, "Kafka consumer will consume the message from kafka topic :" + topic);
		
		initSystemProp();
		boolean enableKafkaListner = Boolean.valueOf(KafkaHelper.getConfigValue(0, "kafka.consumer.inventory"));
		if (enableKafkaListner) {
			InventoryConsumerImpl inventory = new InventoryConsumerImpl(false, topic);
			inventory.setKafkaConfiguration(getCustomPropertyConfig());
			Thread t1 = new Thread(inventory);
			t1.start();
		}
		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.exiting(CLASS_NAME, methodName);
	}

	/**
	 * Override the default config settings.
	 * 
	 * @return
	 */
	private Properties getCustomPropertyConfig() {
		String host = KafkaHelper.getConfigValue(0, "kafka.consumer.inventory.host");
		String port = KafkaHelper.getConfigValue(0, "kafka.consumer.inventory.port");
		String bootstrapServers = host + ":" + port;
				
		Properties properties = new Properties();
		properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "HCLC-1");
		properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		return properties;
	}

	@Override
	public void refresh() throws Exception {
	}
}
