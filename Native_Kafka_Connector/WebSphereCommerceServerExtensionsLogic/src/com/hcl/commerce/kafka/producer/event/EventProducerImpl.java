package com.hcl.commerce.kafka.producer.event;
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

import static com.hcl.commerce.kafka.constants.KafkaConstants.KAFKA_CONFIG;
import static com.hcl.commerce.kafka.constants.KafkaConstants.KAFKA_HOST;
import static com.hcl.commerce.kafka.constants.KafkaConstants.KAFKA_PORT;
import java.util.Properties;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import com.hcl.commerce.kafka.helper.KafkaHelper;
import com.ibm.commerce.cf.kafka.MessageProducer;
import com.ibm.commerce.exception.ECApplicationException;
import com.ibm.commerce.foundation.logging.LoggingHelper;
import com.ibm.commerce.ras.ECMessage;
import com.ibm.commerce.ras.ECMessageHelper;

/**
 * This is class provides the capability to create the Kfaka connection and
 * publish the data on kafka topic.
 * 
 * @author hcladmin
 * @param <T>
 *
 */

public class EventProducerImpl<T> implements EventProducer<T> {
	private static final String CLASS_NAME = "ProducerEventImpl";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	/**
	 * This method provides the implementation to publish the data on kafka topic.
	 * 
	 * @throws ECApplicationException
	 */
	@Override
	public void fireEvent(T event) throws ECApplicationException {
		final String methodName = "fireEvent";
		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.entering(CLASS_NAME, methodName, event);

		String host = KafkaHelper.getConfigValue(0, KAFKA_HOST);
		String port = KafkaHelper.getConfigValue(0, KAFKA_PORT);
		String bootstrapServers = host + ":" + port;
		String topicName = KafkaHelper.getValueByConfigGroupingNameAndPropertyName(KAFKA_CONFIG,
				"com.hcl.kafka.producer.order.topic");

		if (StringUtils.isEmpty(bootstrapServers)) {
			throwException(bootstrapServers);
		}
		if (StringUtils.isEmpty(topicName)) {
			throwException(topicName);
		}
		Properties properties = new Properties();
		properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		MessageProducer producer = new MessageProducer();
		producer.start(properties, false);
		producer.send(false, false, topicName, null, event.toString());

		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.exiting(CLASS_NAME, methodName);
	}

	private void throwException(String param) throws ECApplicationException {
		throw new ECApplicationException(ECMessage._ERR_MISSING_PARAMETER, this.getClass().getName(), "throwException",
				ECMessageHelper.generateMsgParms("Parameter value can`t be null or empty." + param));
	}

}
