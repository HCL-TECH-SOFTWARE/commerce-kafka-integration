package com.hcl.commerce.inventory.objects;

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
