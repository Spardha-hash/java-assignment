package com.fulfilment.application.monolith.fulfillment.adapters.event;

import com.fulfilment.application.monolith.fulfillment.domain.event.FulfillmentChangedEvent;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentEventPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;

/** Publishes domain events on the CDI event bus. */
@ApplicationScoped
public class CdiFulfillmentEventPublisher implements FulfillmentEventPublisher {

  private final Event<FulfillmentChangedEvent> cdiEvent;

  public CdiFulfillmentEventPublisher(Event<FulfillmentChangedEvent> cdiEvent) {
    this.cdiEvent = cdiEvent;
  }

  @Override
  public void publish(FulfillmentChangedEvent event) {
    cdiEvent.fire(event);
  }
}
