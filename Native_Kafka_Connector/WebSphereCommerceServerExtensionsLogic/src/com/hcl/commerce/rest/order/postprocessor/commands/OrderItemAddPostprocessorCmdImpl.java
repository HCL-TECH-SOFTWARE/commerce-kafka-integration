package com.hcl.commerce.rest.order.postprocessor.commands;
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


import static com.hcl.commerce.kafka.constants.KafkaConstants.ORDER_ID;
import static com.hcl.commerce.kafka.constants.KafkaConstants.ORDERITEM_IDS;
import static com.hcl.commerce.kafka.constants.KafkaConstants.EVENT_NAME;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hcl.commerce.kafka.helper.KafkaHelper;
import com.hcl.commerce.kafka.producer.event.EventProducer;
import com.hcl.commerce.kafka.producer.event.EventProducerImpl;
import com.ibm.commerce.command.ControllerCommandImpl;
import com.ibm.commerce.exception.ECException;

/**
 * @author hcladmin
 *
 */
public class OrderItemAddPostprocessorCmdImpl extends ControllerCommandImpl implements OrderItemAddPostprocessorCmd {
	private static final long serialVersionUID = 1L;
	private static final String CLASS_NAME = OrderItemAddPostprocessorCmdImpl.class.getName();
	private static final Logger LOGGER = LoggerFactory.getLogger(CLASS_NAME);

	@Override
	public void performExecute() throws ECException {
		LOGGER.info("Entry", CLASS_NAME);
		try {
			boolean enableKafkaProducer = Boolean.valueOf(KafkaHelper.getConfigValue(0, "kafka.producer.orderitemadd"));
			if (enableKafkaProducer) {
				HttpServletRequest request = (HttpServletRequest) requestProperties.get("request");
				Map<String, Object> bodyMap = (Map<String, Object>) request.getAttribute("resolvedBodyParams");

				Response originResponse = (Response) request.getAttribute("originResponse");
				Map<String, Object> responseMap = (Map<String, Object>) originResponse.getEntity();

				EventProducer<JSONObject> e = new EventProducerImpl<JSONObject>();
				Map<String, Object> resultData = (Map<String, Object>) responseMap.get("resultData");
				List<Map> orderItems = (List<Map>) resultData.get("orderItem");

				List<String> pks = new ArrayList<String>();
				for (Map<String, String> orderItem : orderItems) {
					pks.add(orderItem.get("orderItemId"));
				}

				JSONObject orderData = new JSONObject();
				orderData.put(EVENT_NAME, "OrderItemAdd");
				orderData.put(ORDER_ID, resultData.get("orderId"));
				orderData.put(ORDERITEM_IDS, pks);
				e.fireEvent(orderData);
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		LOGGER.info("Exit", CLASS_NAME);
	}
}
