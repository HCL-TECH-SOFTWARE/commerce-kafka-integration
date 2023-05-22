package com.hcl.commerce.kafka.helper;
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

import static com.hcl.commerce.kafka.constants.KafkaConstants.CATENTRY_ID;
import static com.hcl.commerce.kafka.constants.KafkaConstants.FFM_CENTER_ID;
import static com.hcl.commerce.kafka.constants.KafkaConstants.INVENTORY_FLAG;
import static com.hcl.commerce.kafka.constants.KafkaConstants.QUANTITY;
import static com.hcl.commerce.kafka.constants.KafkaConstants.QUANTITY_MEASURE;
import static com.hcl.commerce.kafka.constants.KafkaConstants.STORE_ID;

import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.json.JSONObject;

import com.hcl.commerce.inventory.objects.Inventory;
import com.ibm.commerce.base.objects.ServerJDBCHelperBean;
import com.ibm.commerce.emarketing.utils.EncryptHelper;
import com.ibm.commerce.foundation.common.config.ComponentConfiguration;
import com.ibm.commerce.foundation.common.config.ComponentConfigurationRegistry;
import com.ibm.commerce.foundation.internal.server.services.registry.StoreConfigurationRegistry;
import com.ibm.commerce.foundation.logging.LoggingHelper;

public class KafkaHelper {
	private static final String CLASS_NAME = "CommerceHelper";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	public static final String SQL_INSERT__DATA = "INSERT INTO WCS.INVENTORY\n"
			+ "(CATENTRY_ID, QUANTITY, FFMCENTER_ID, STORE_ID, QUANTITYMEASURE, INVENTORYFLAGS, OPTCOUNTER)\n"
			+ "VALUES(?, ?, ?, ?, ?, ?, ?)";

	/**
	 * This method prepares the auth token for provided user and password.
	 * 
	 * @param userName
	 * @param password
	 * @return
	 */
	public static String getAuthToken(String userName, String password) {
		final String methodName = "getAuthToken";
		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.entering(CLASS_NAME, methodName, new Object[] { userName, password });

		String encoded = Base64.getEncoder()
				.encodeToString((userName + ":" + password).getBytes(StandardCharsets.UTF_8));
		String authHeader = "Basic " + new String(encoded);

		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.exiting(CLASS_NAME, methodName);
		return authHeader;
	}

	/**
	 * This method provides capability to fetch data from wc-component.xml .
	 * 
	 * @param astrConfigGroupingName
	 * @param astrName
	 * @return
	 */
	public static final String getValueByConfigGroupingNameAndPropertyName(String astrConfigGroupingName,
			String astrName) {
		final String methodName = "getValueByConfigGroupingNameAndPropertyName";
		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.entering(CLASS_NAME, methodName, new Object[] { astrConfigGroupingName, astrName });

		ComponentConfiguration config = ComponentConfigurationRegistry.instance()
				.getComponentConfiguration("com.ibm.commerce.foundation-ext");
		String value = config.getValueByConfigGroupingNameAndPropertyName(astrConfigGroupingName, astrName);

		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.exiting(CLASS_NAME, methodName);
		return value;

	}

	/**
	 * This method provides the capability to insert the record in inventory table.
	 * 
	 * @param inv
	 * @throws SQLException
	 * @throws NamingException
	 */
	public void createInventoryByJDBC(Inventory[] inv) throws NamingException, SQLException {
		final String methodName = "createInventoryByJDBC";
		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.entering(CLASS_NAME, methodName, inv);

		for (int i = 0; i < inv.length; i++) {
			String[] parameters = new String[7];
			parameters[0] = String.valueOf(inv[i].getCatalogEntryId());
			parameters[1] = String.valueOf(inv[i].getQuantity());
			parameters[2] = String.valueOf(inv[i].getFulfillmentCenterId());
			parameters[3] = String.valueOf(inv[i].getStoreId());
			parameters[4] = String.valueOf(inv[i].getQuantityMeasure());
			parameters[5] = String.valueOf(inv[i].getInventoryFlags());
			parameters[6] = "1";
			ServerJDBCHelperBean jdbc = new ServerJDBCHelperBean();
			jdbc.executeParameterizedUpdate(SQL_INSERT__DATA, parameters);
		}
		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.exiting(CLASS_NAME, methodName);
	}

	/**
	 * This method provides implementation to save the inventory record by calling
	 * inventory API.
	 * 
	 * @param inv
	 */
	public void createInventoryByRestApi(Inventory[] inv, URL url) {
		final String methodName = "createInventoryByRestApi";
		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.entering(CLASS_NAME, methodName, inv);

		try {
			String userName = KafkaHelper.getValueByConfigGroupingNameAndPropertyName("RestConfig", "username");
			String password = KafkaHelper.getValueByConfigGroupingNameAndPropertyName("RestConfig", "password");
			String decryptedPassword = EncryptHelper.decrypt(password);
			String authHeader = KafkaHelper.getAuthToken(userName, decryptedPassword);
			disableSSL();
			for (int i = 0; i < inv.length; i++) {
				JSONObject requestBody = new JSONObject();
				requestBody.put(CATENTRY_ID, inv[i].getCatalogEntryId());
				requestBody.put(QUANTITY, inv[i].getQuantity());
				requestBody.put(QUANTITY_MEASURE, inv[i].getQuantityMeasure());
				requestBody.put(INVENTORY_FLAG, "0");
				requestBody.put(STORE_ID, inv[i].getStoreId());
				requestBody.put(FFM_CENTER_ID, inv[i].getFulfillmentCenterId());

				HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
				con.setRequestMethod("POST");
				con.setRequestProperty("Authorization", authHeader);
				con.setRequestProperty("Content-Type", "application/json");
				con.setDoOutput(true);
				OutputStream os = con.getOutputStream();
				os.write(requestBody.toString().getBytes());
				os.flush();
				os.close();
				int responseCode = con.getResponseCode();
				LOGGER.log(Level.INFO, "Records has been created sucessfully response code :"+responseCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.exiting(CLASS_NAME, methodName);
	}

	/**
	 * This method is used to get the value from STORECONF for the name provided.
	 * 
	 * @param anStoreId
	 * @param name
	 * @return
	 */
	public static final String getConfigValue(Integer anStoreId, String name) {
		final String methodName = "getConfigValue";
		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.entering(CLASS_NAME, methodName, new Object[] { anStoreId, name });

		StoreConfigurationRegistry storeConfigurationRegistry = StoreConfigurationRegistry.getSingleton();
		String value = storeConfigurationRegistry.getValue(anStoreId, name);

		if (LoggingHelper.isEntryExitTraceEnabled(LOGGER))
			LOGGER.exiting(CLASS_NAME, methodName);
		return value;

	}

	private void disableSSL() throws NoSuchAlgorithmException, KeyManagementException {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };

		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}
}
