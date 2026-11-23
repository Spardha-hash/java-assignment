package com.fulfilment.application.monolith.fulfillment.domain.models;

/**
 * States that a Warehouse (identified by its business unit code, which survives warehouse
 * replacement) fulfils a Product for a Store. Plain domain object: no framework annotations.
 */
public class FulfillmentAssignment {

  public Long id;

  public Long storeId;

  public Long productId;

  public String warehouseBusinessUnitCode;

  public FulfillmentAssignment() {}

  public FulfillmentAssignment(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    this.storeId = storeId;
    this.productId = productId;
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
  }
}
