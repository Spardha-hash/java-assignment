package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import com.fulfilment.application.monolith.fulfillment.domain.ports.ListFulfillmentOperation;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ListFulfillmentUseCase implements ListFulfillmentOperation {

  private final FulfillmentStore store;

  public ListFulfillmentUseCase(FulfillmentStore store) {
    this.store = store;
  }

  @Override
  public List<FulfillmentAssignment> listByStore(Long storeId) {
    return store.findByStore(storeId);
  }
}
