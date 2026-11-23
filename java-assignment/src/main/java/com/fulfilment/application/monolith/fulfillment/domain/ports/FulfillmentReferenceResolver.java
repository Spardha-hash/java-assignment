package com.fulfilment.application.monolith.fulfillment.domain.ports;

/** Outbound port: checks that the things an association points to really exist. */
public interface FulfillmentReferenceResolver {

  boolean storeExists(Long storeId);

  boolean productExists(Long productId);

  boolean activeWarehouseExists(String warehouseBusinessUnitCode);
}
