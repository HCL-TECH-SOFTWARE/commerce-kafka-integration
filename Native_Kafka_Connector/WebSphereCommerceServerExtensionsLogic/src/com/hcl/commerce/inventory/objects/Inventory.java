package com.hcl.commerce.inventory.objects;

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

public class Inventory {
	private Long catalogEntryId;
	private Double quantity;
	private String quantityMeasure;
	private Integer inventoryFlags;
	private Integer storeId;
	private Integer fulfillmentCenterId;

	public Long getCatalogEntryId() {
		return catalogEntryId;
	}

	public void setCatalogEntryId(Long catalogEntryId) {
		this.catalogEntryId = catalogEntryId;
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	public String getQuantityMeasure() {
		return quantityMeasure;
	}

	public void setQuantityMeasure(String quantityMeasure) {
		this.quantityMeasure = quantityMeasure;
	}

	public Integer getInventoryFlags() {
		return inventoryFlags;
	}

	public void setInventoryFlags(Integer inventoryFlags) {
		this.inventoryFlags = inventoryFlags;
	}

	public Integer getStoreId() {
		return storeId;
	}

	public void setStoreId(Integer storeId) {
		this.storeId = storeId;
	}

	public Integer getFulfillmentCenterId() {
		return fulfillmentCenterId;
	}

	public void setFulfillmentCenterId(Integer fulfillmentCenterId) {
		this.fulfillmentCenterId = fulfillmentCenterId;
	}

}
