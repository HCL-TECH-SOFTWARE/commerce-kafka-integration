# HCL Commerce and Kafka Messaging Integration Asset

# WARRANTY & SUPPORT

HCL Software provides HCL Commerce open source assets “as-is” without obligation to support them nor warranties or any kind, either express or implied, including the warranty of title, non-infringement or non-interference, and the implied warranties and conditions of merchantability and fitness for a particular purpose. HCL Commerce open source assets are not covered under the HCL Commerce master license nor Support contracts.

If you have questions or encounter problems with an HCL Commerce open source asset, please open an issue in the asset's GitHub repository. For more information about GitHub issues, including creating an issue, please refer to GitHub Docs. The HCL Commerce Innovation Factory Team, who develops HCL Commerce open source assets, monitors GitHub issues and will do their best to address them.

# HCLC-Kafka Integration
This PoC is about the integration of Kafka with HCL Commerce where Customer can place the publish and consume the message to and from Kafka topic. So for this PoC we have provided the one sample of producer which is publishing the orderitem details on Kafka topic on trigger of orderitemadd event. For consumer example we have created a consumer class which is listening the publish event on Inventoty topic. So once publish event will occure on inventory topic consumer class will comsume the message and will save it into Inventoty table in database.

### For Integration details please refer the Native_Kafka_Connector.doc under this Native_Kafka_Connector folder.

# HCL Commerce Version compatibity
This asset has been tested with HCL Commerce v9.1.11 and Kafka 3.0.0
