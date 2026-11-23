package com.fulfilment.application.monolith.fulfillment.domain.ports;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;

/** Inbound port: link a warehouse to a product for a store. */
public interface AssignFulfillmentOperation {

  FulfillmentAssignment assign(FulfillmentAssignment assignment);
}
