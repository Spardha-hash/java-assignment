package com.fulfilment.application.monolith.fulfillment.domain.ports;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;
import java.util.List;

/** Outbound port: persistence of fulfilment associations. */
public interface FulfillmentStore {

  FulfillmentAssignment findAssignmentById(Long id);

  List<FulfillmentAssignment> findByStore(Long storeId);

  List<FulfillmentAssignment> findByStoreAndProduct(Long storeId, Long productId);

  List<FulfillmentAssignment> findByWarehouse(String warehouseBusinessUnitCode);

  boolean exists(Long storeId, Long productId, String warehouseBusinessUnitCode);

  void create(FulfillmentAssignment assignment);

  void remove(FulfillmentAssignment assignment);
}
