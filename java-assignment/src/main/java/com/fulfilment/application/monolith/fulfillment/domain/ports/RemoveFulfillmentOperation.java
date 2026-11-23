package com.fulfilment.application.monolith.fulfillment.domain.ports;

/** Inbound port: remove an association. */
public interface RemoveFulfillmentOperation {

  void remove(Long id);
}
