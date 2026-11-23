package com.fulfilment.application.monolith.fulfillment.domain.ports;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;
import java.util.List;

/** Inbound port: list the associations of a store. */
public interface ListFulfillmentOperation {

  List<FulfillmentAssignment> listByStore(Long storeId);
}
