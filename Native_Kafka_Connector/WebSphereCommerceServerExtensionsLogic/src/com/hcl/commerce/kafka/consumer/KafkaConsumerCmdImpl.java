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
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcl.commerce.inventory.objects.Inventory;
import com.hcl.commerce.kafka.helper.KafkaHelper;
import com.ibm.commerce.command.ControllerCommandImpl;
import com.ibm.commerce.exception.ECException;
import com.ibm.commerce.foundation.logging.LoggingHelper;
import com.ibm.commerce.fulfillment.objects.InventoryAccessBean;
import com.ibm.commerce.fulfillment.objects.InventoryEntityCreationData;

/**
 * This class is used to fetch the data from kafka consumer and save it into DB.
 *
 */
public class KafkaConsumerCmdImpl extends ControllerCommandImpl implements KafkaConsumerCmd {
	private static final long serialVersionUID = 1L;
	private static final String CLASS_NAME = "InventoryRestHelper";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	@Override
	public void performExecute() throws ECException {
		final String methodName = "performExecute()";
		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.entering(CLASS_NAME, methodName);

		super.performExecute();
		boolean enableKafkaListner = Boolean.valueOf(KafkaHelper.getConfigValue(0, "kafka.consumer.inventory"));

		if (enableKafkaListner) {
			Properties properties = getKafkaConfig();
			String topic = KafkaHelper.getValueByConfigGroupingNameAndPropertyName("SchedulerConfig",
					"com.hcl.kafka.consumer.inventory.topic");
			KafkaMessageConsumer consumer = new KafkaMessageConsumer(properties, topic);
			Thread t1 = new Thread(consumer);
			t1.run();
			List<ConsumerRecord<String, String>> records = null;
			if (consumer.getRecord() != null) {
				records = consumer.getRecord();
			}
			if (records != null) {
				LOGGER.log(Level.INFO,
						"####Scheduler class has recieved these messages from kafka consumer :" + records);
				createInventory(records);
			}
		}
		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.exiting(CLASS_NAME, methodName);
	}

	/**
	 * This method prepares the config properties used by kafka consumer.
	 * 
	 * @return
	 */
	private Properties getKafkaConfig() {
		final String methodName = "getKafkaConfig()";
		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.entering(CLASS_NAME, methodName);

		String host = KafkaHelper.getConfigValue(0, "kafka.consumer.inventory.host");
		String port = KafkaHelper.getConfigValue(0, "kafka.consumer.inventory.port");
		String bootstrapServers = host + ":" + port;
		
		Properties properties = new Properties();
		properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "HCLC-2");
		properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.exiting(CLASS_NAME, methodName);
		return properties;
	}

	/**
	 * Saves the inventory kafka message into inventory table in commerce.
	 * 
	 * @param records
	 */
	private void createInventory(List<ConsumerRecord<String, String>> records) {
		final String methodName = "createInventory()";
		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.entering(CLASS_NAME, methodName);
		for (ConsumerRecord<String, String> record : records) {
			try {
				ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
						false);
				Inventory[] inv = mapper.readValue(record.value().toString(), Inventory[].class);
				for (int i = 0; i < inv.length; i++) {
					InventoryEntityCreationData invEntity = new InventoryEntityCreationData();
					invEntity.setCatalogEntryId(inv[i].getCatalogEntryId());
					invEntity.setFulfillmentCenterId(inv[i].getFulfillmentCenterId());
					invEntity.setQuantity(inv[i].getQuantity());
					invEntity.setQuantityMeasure(inv[i].getQuantityMeasure());
					invEntity.setStoreId(inv[i].getStoreId());
					invEntity.setInventoryFlags(0);
					InventoryAccessBean inventory = new InventoryAccessBean(invEntity);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.exiting(CLASS_NAME, methodName);
	}

}
