package com.fulfilment.application.monolith.fulfillment.domain.exceptions;

/** A fulfilment association, store, product or warehouse that was referenced does not exist. */
public class FulfillmentNotFoundException extends FulfillmentException {
  public FulfillmentNotFoundException(String message) {
    super(message);
  }
}
