package com.fulfilment.application.monolith.fulfillment.adapters.event;

import com.fulfilment.application.monolith.fulfillment.domain.event.FulfillmentChangedEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import org.jboss.logging.Logger;

/**
 * Reacts to fulfilment changes only after the database transaction committed. Here it only writes
 * an audit log line; it is the place to hook notifications or integrations.
 */
@ApplicationScoped
public class FulfillmentEventListener {

  private static final Logger LOG = Logger.getLogger(FulfillmentEventListener.class);

  void onChanged(@Observes(during = TransactionPhase.AFTER_SUCCESS) FulfillmentChangedEvent event) {
    LOG.infof(
        "Fulfillment %s: id=%s store=%s product=%s warehouse=%s",
        event.type(), event.id(), event.storeId(), event.productId(),
        event.warehouseBusinessUnitCode());
  }
}
