package com.fulfilment.application.monolith.fulfillment.domain.ports;

import com.fulfilment.application.monolith.fulfillment.domain.event.FulfillmentChangedEvent;

/** Outbound port: tells the rest of the system that an association changed. */
public interface FulfillmentEventPublisher {

  void publish(FulfillmentChangedEvent event);
}
