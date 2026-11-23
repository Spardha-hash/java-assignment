package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import com.fulfilment.application.monolith.fulfillment.domain.event.FulfillmentChangedEvent;
import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;
import com.fulfilment.application.monolith.fulfillment.domain.ports.AssignFulfillmentOperation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentEventPublisher;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import com.fulfilment.application.monolith.fulfillment.domain.validators.FulfillmentValidator;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AssignFulfillmentUseCase implements AssignFulfillmentOperation {

  private final FulfillmentStore store;
  private final FulfillmentValidator validator;
  private final FulfillmentEventPublisher events;

  public AssignFulfillmentUseCase(
      FulfillmentStore store, FulfillmentValidator validator, FulfillmentEventPublisher events) {
    this.store = store;
    this.validator = validator;
    this.events = events;
  }

  @Override
  public FulfillmentAssignment assign(FulfillmentAssignment assignment) {
    validator.validateForAssignment(assignment);

    store.create(assignment);

    events.publish(
        new FulfillmentChangedEvent(
            FulfillmentChangedEvent.Type.ASSIGNED,
            assignment.id,
            assignment.storeId,
            assignment.productId,
            assignment.warehouseBusinessUnitCode));
    return assignment;
  }
}
