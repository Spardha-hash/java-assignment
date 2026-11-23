package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import com.fulfilment.application.monolith.fulfillment.domain.event.FulfillmentChangedEvent;
import com.fulfilment.application.monolith.fulfillment.domain.exceptions.FulfillmentNotFoundException;
import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentEventPublisher;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import com.fulfilment.application.monolith.fulfillment.domain.ports.RemoveFulfillmentOperation;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RemoveFulfillmentUseCase implements RemoveFulfillmentOperation {

  private final FulfillmentStore store;
  private final FulfillmentEventPublisher events;

  public RemoveFulfillmentUseCase(FulfillmentStore store, FulfillmentEventPublisher events) {
    this.store = store;
    this.events = events;
  }

  @Override
  public void remove(Long id) {
    FulfillmentAssignment assignment = id == null ? null : store.findAssignmentById(id);
    if (assignment == null) {
      throw new FulfillmentNotFoundException("Fulfillment " + id + " does not exist.");
    }

    store.remove(assignment);

    events.publish(
        new FulfillmentChangedEvent(
            FulfillmentChangedEvent.Type.REMOVED,
            assignment.id,
            assignment.storeId,
            assignment.productId,
            assignment.warehouseBusinessUnitCode));
  }
}
