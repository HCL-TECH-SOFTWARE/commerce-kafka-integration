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

import static com.hcl.commerce.kafka.constants.KafkaConstants.KAFKA_CONFIG;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcl.commerce.inventory.objects.Inventory;
import com.hcl.commerce.kafka.helper.KafkaHelper;
import com.ibm.commerce.cf.kafka.MessageConsumer;
import com.ibm.commerce.component.util.BackendServersConfigurationUtil;
import com.ibm.commerce.foundation.internal.server.services.search.config.solr.SolrSearchConfigurationRegistry;
import com.ibm.commerce.foundation.logging.LoggingHelper;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/**
 * This class provides a capability to fetch the message from kafka consumer and
 * process it further.
 *
 */
public class InventoryConsumerImpl extends MessageConsumer {
	private static final String CLASS_NAME = "InventoryConsumerImpl";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	private Inventory[] inv;
	private final String ADD_INVENTORY_API_URL = "/rest/admin/v2/inventories";
	private final String REST_CALL ="enableRestCall";
	private final String JDBC_CALL = "enableJdbcCall";

	public InventoryConsumerImpl(boolean enableTopicPrefix, String topic) {
		super(enableTopicPrefix, topic);
	}

	/**
	 * This method fetches the kafka message from kafka consumer class and process
	 * it further.
	 * 
	 */
	public void action(ConsumerRecord<String, String> consumerRecord) {
		final String methodName = "action";
		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.entering(CLASS_NAME, methodName, consumerRecord);
		LOGGER.log(Level.INFO, "#####Kafka message recieved :" + consumerRecord);
		try {
			boolean enableRestCall = Boolean.valueOf(KafkaHelper.getValueByConfigGroupingNameAndPropertyName(
					KAFKA_CONFIG, REST_CALL));
			boolean enableJdbcCall = Boolean.valueOf(KafkaHelper.getValueByConfigGroupingNameAndPropertyName(
					KAFKA_CONFIG, JDBC_CALL));
			
			ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
			this.inv = mapper.readValue(consumerRecord.value().toString(), Inventory[].class);
			
			if (enableRestCall) {
				createInventoryByRestApi(this.inv);
			}
			if (enableJdbcCall) {
				createInventoryByJDBC(this.inv);
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.exiting(CLASS_NAME, methodName);
	}

	/**
	 * This is used to create new inventory record in INVENTORY table using REST API
	 * call.
	 * 
	 * @param inv
	 * @throws MalformedURLException
	 */
	public void createInventoryByRestApi(Inventory[] inv) throws MalformedURLException {
		final String methodName = "createInventoryByRestApi";
		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.entering(CLASS_NAME, methodName, inv);
		String hostName = SolrSearchConfigurationRegistry.getInstance().getSearchServerHostname();
		String port = BackendServersConfigurationUtil.getWCSSecurePort();
		URL url = new URL("https://" + hostName + ":" + port + ADD_INVENTORY_API_URL);

		new KafkaHelper().createInventoryByRestApi(inv, url);

		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.exiting(CLASS_NAME, methodName);
	}

	/**
	 * This method is used to create new inventory record in database table
	 * INVENTORY using JDBC call.
	 * 
	 * @param inv
	 * @throws SQLException
	 * @throws NamingException
	 */
	public void createInventoryByJDBC(Inventory[] inv) throws SQLException, NamingException {
		final String methodName = "createInventoryByJDBC";
		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.entering(CLASS_NAME, methodName, inv);

		new KafkaHelper().createInventoryByJDBC(inv);

		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.exiting(CLASS_NAME, methodName);
	}
}
