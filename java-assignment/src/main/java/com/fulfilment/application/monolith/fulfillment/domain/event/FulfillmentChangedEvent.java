package com.fulfilment.application.monolith.fulfillment.domain.event;

/** Domain event: an association was created or removed. */
public record FulfillmentChangedEvent(
    Type type, Long id, Long storeId, Long productId, String warehouseBusinessUnitCode) {

  public enum Type {
    ASSIGNED,
    REMOVED
  }
}
